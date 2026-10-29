package com.xerago.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValueValidator implements ConstraintValidator<ValidEnum, String> {

    private Enum<?>[] constants;

    @Override
    public void initialize(ValidEnum annotation) {
        constants = annotation.value().getEnumConstants();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String input = value.trim();
        boolean valid = Arrays.stream(constants)
                .anyMatch(c -> c.name().equalsIgnoreCase(input) || c.toString().equalsIgnoreCase(input));
        if (!valid) {
            String allowed = String.join(", ", Arrays.stream(constants).map(Object::toString).toList());
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Invalid value '" + input.replace("{", "").replace("$", "") + "'. Allowed values: " + allowed + ".")
                    .addConstraintViolation();
        }
        return valid;
    }
}
