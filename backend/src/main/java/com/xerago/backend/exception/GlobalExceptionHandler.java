package com.xerago.backend.exception;

import com.xerago.backend.dto.ErrorResponseDto;
import com.xerago.backend.dto.ErrorResponseDto.FieldErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<Object> handlePolicyNotFound(PolicyNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null, request);
    }

    @ExceptionHandler(DuplicatePolicyException.class)
    public ResponseEntity<Object> handleDuplicatePolicy(DuplicatePolicyException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(),
                List.of(new FieldErrorDto("policyNumber", "Policy number already exists")), request);
    }

    @ExceptionHandler(InvalidSortException.class)
    public ResponseEntity<Object> handleInvalidSort(InvalidSortException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(),
                List.of(new FieldErrorDto("sort", "Unsupported sort field")), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());

        String cause = String.valueOf(ex.getMostSpecificCause().getMessage());
        if (cause.contains("uk_policies_policy_number") || cause.contains("Duplicate entry")) {
            return build(HttpStatus.CONFLICT, "Policy number already exists.",
                    List.of(new FieldErrorDto("policyNumber", "Policy number already exists")), request);
        }
        return build(HttpStatus.BAD_REQUEST, "The request violates a data constraint.", null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Unhandled exception on {}", path(request), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.",
                null, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<FieldErrorDto> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::toFieldError)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers, HttpStatusCode status,
                                                                            WebRequest request) {
        List<FieldErrorDto> errors = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result -> {
            if (result instanceof ParameterErrors parameterErrors) {
                parameterErrors.getFieldErrors().forEach(e -> errors.add(toFieldError(e)));
            } else {
                String name = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(e ->
                        errors.add(new FieldErrorDto(name, e.getDefaultMessage())));
            }
        });
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        if (ex.getCause() instanceof InvalidFormatException invalid) {
            String field = invalid.getPath().isEmpty() ? null
                    : invalid.getPath().getLast().getPropertyName();
            Class<?> target = invalid.getTargetType();
            String message = target != null && target.isEnum()
                    ? "Invalid value '" + invalid.getValue() + "'. Allowed values: " + allowedValues(target) + "."
                    : target == java.time.LocalDate.class
                    ? "Invalid date '" + invalid.getValue() + "'. Use the format yyyy-MM-dd."
                    : "Invalid value '" + invalid.getValue() + "'"
                    + (target != null ? " (expected " + target.getSimpleName() + ")." : ".");
            return build(HttpStatus.BAD_REQUEST, "Malformed request body",
                    List.of(new FieldErrorDto(field, message)), request);
        }
        return build(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body.", null, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(org.springframework.beans.TypeMismatchException ex,
                                                        HttpHeaders headers, HttpStatusCode status,
                                                        WebRequest request) {
        String name = ex instanceof MethodArgumentTypeMismatchException mismatch ? mismatch.getName()
                : ex.getPropertyName();
        Class<?> type = ex.getRequiredType();
        String message = "Invalid value '" + ex.getValue() + "'"
                + (type != null && type.isEnum() ? ". Allowed values: " + allowedValues(type) + "."
                : type != null ? " (expected " + type.getSimpleName() + ")." : ".");
        return build(HttpStatus.BAD_REQUEST, "Invalid request parameter",
                List.of(new FieldErrorDto(name, message)), request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("Server error on {}", path(request), ex);
        }
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String message = statusCode.is5xxServerError()
                ? "An unexpected error occurred. Please try again later."
                : ex.getMessage();
        ErrorResponseDto dto = new ErrorResponseDto(LocalDateTime.now(), statusCode.value(),
                status != null ? status.getReasonPhrase() : "Error", message, path(request), null);
        return new ResponseEntity<>(dto, headers, statusCode);
    }

    private static ResponseEntity<Object> build(HttpStatus status, String message,
                                                List<FieldErrorDto> fieldErrors, WebRequest request) {
        ErrorResponseDto dto = new ErrorResponseDto(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), message, path(request), fieldErrors);
        return ResponseEntity.status(status).body(dto);
    }

    private static FieldErrorDto toFieldError(FieldError error) {
        return new FieldErrorDto(error.getField(), error.getDefaultMessage());
    }

    private static String path(WebRequest request) {
        return request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI()
                : request.getDescription(false);
    }

    private static String allowedValues(Class<?> enumType) {
        return String.join(", ", Arrays.stream(enumType.getEnumConstants()).map(Object::toString).toList());
    }
}
