import { useId } from 'react';
import { ChevronDownIcon } from './Icons';

export function Field({ label, required, error, hint, className = '', children }) {
  const id = useId();
  return (
    <div className={`field ${className}`}>
      <label className="field-label" htmlFor={id}>
        {label}
        {required && <span className="field-required"> *</span>}
      </label>
      {children({ id, 'aria-invalid': Boolean(error), 'aria-describedby': error ? `${id}-error` : undefined })}
      {error ? (
        <span className="field-error" id={`${id}-error`} role="alert">
          {error}
        </span>
      ) : (
        hint && <span className="field-hint">{hint}</span>
      )}
    </div>
  );
}

export function Control({ invalid, prefix, icon, children, trailing }) {
  return (
    <div className={`control ${invalid ? 'control-invalid' : ''}`}>
      {prefix && <span className="control-affix">{prefix}</span>}
      {icon && <span className="control-icon">{icon}</span>}
      {children}
      {trailing}
    </div>
  );
}

export function TextInput({ prefix, icon, trailing, ...props }) {
  return (
    <Control invalid={props['aria-invalid']} prefix={prefix} icon={icon} trailing={trailing}>
      <input {...props} />
    </Control>
  );
}

export function SelectInput({ options, placeholder, icon, ...props }) {
  return (
    <Control invalid={props['aria-invalid']} icon={icon} trailing={<ChevronDownIcon size={18} className="select-chevron" />}>
      <select {...props}>
        {placeholder !== undefined && <option value="">{placeholder}</option>}
        {options.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </select>
    </Control>
  );
}
