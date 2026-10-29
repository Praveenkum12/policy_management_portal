import { useEffect, useRef, useState } from 'react';
import { POLICY_NUMBER_PREFIX, POLICY_STATUSES, POLICY_TYPES } from '../../constants/policy';
import { EMPTY_POLICY_FORM, toFormValues, toRequestBody, validatePolicy } from '../../utils/policyValidation';
import { Button, ErrorBanner, Field, Modal, SelectInput, TextInput } from '../common';

export default function PolicyFormModal({ policy, onClose, onSubmit }) {
  const isEdit = Boolean(policy);
  const [values, setValues] = useState(isEdit ? toFormValues(policy) : EMPTY_POLICY_FORM);
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState(null);
  const [saving, setSaving] = useState(false);
  const firstFieldRef = useRef(null);

  useEffect(() => {
    firstFieldRef.current?.focus();
  }, []);

  function change(name, value) {
    setValues((current) => ({ ...current, [name]: value }));
    setErrors((current) => ({ ...current, [name]: undefined }));
  }

  function validateField(name) {
    const fieldError = validatePolicy(values)[name];
    setErrors((current) => ({ ...current, [name]: fieldError }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const validation = validatePolicy(values);
    setErrors(validation);
    setSubmitError(null);
    if (Object.keys(validation).length > 0) return;

    setSaving(true);
    try {
      await onSubmit(toRequestBody(values));
    } catch (err) {
      const serverErrors = Object.fromEntries((err.fieldErrors ?? []).map((e) => [e.field, e.message]));
      setErrors(serverErrors);
      setSubmitError(Object.keys(serverErrors).length > 0 ? 'Please fix the highlighted fields.' : err.message);
      setSaving(false);
    }
  }

  const bind = (name) => ({
    name,
    value: values[name],
    onBlur: () => validateField(name),
    onChange: (event) => change(name, event.target.value),
  });

  return (
    <Modal title={isEdit ? 'Edit policy' : 'New policy'} onClose={onClose} dismissible={!saving}>
      <form onSubmit={handleSubmit} noValidate>
        <div className="modal-body">
          {submitError && <ErrorBanner message={submitError} />}
          {isEdit && <p className="form-meta">Policy ID: {policy.id}</p>}

          <div className="form-grid">
            <Field label="Policy number" required error={errors.policyNumber} hint="Digits only, up to 10">
              {(aria) => (
                <TextInput
                  {...aria}
                  {...bind('policyNumber')}
                  ref={firstFieldRef}
                  prefix={POLICY_NUMBER_PREFIX}
                  inputMode="numeric"
                  maxLength={10}
                  autoComplete="off"
                  placeholder="1001"
                  onChange={(event) => change('policyNumber', event.target.value.replace(/\D/g, ''))}
                />
              )}
            </Field>

            <Field label="Customer name" required error={errors.customerName}>
              {(aria) => (
                <TextInput {...aria} {...bind('customerName')} maxLength={120} autoComplete="off" placeholder="Asha Raman" />
              )}
            </Field>

            <Field label="Policy type" required error={errors.policyType}>
              {(aria) => (
                <SelectInput {...aria} {...bind('policyType')} options={POLICY_TYPES} placeholder="Select type" />
              )}
            </Field>

            <Field label="Premium amount (₹)" required error={errors.premiumAmount}>
              {(aria) => (
                <TextInput {...aria} {...bind('premiumAmount')} type="number" inputMode="decimal" min="0.01" step="0.01" placeholder="12500.00" />
              )}
            </Field>

            <Field label="Status" required error={errors.status}>
              {(aria) => (
                <SelectInput {...aria} {...bind('status')} options={POLICY_STATUSES} placeholder="Select status" />
              )}
            </Field>

            <Field label="Start date" required error={errors.startDate}>
              {(aria) => <TextInput {...aria} {...bind('startDate')} type="date" />}
            </Field>
          </div>
        </div>

        <footer className="modal-footer">
          <Button onClick={onClose} disabled={saving}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" loading={saving}>
            {saving ? 'Saving…' : isEdit ? 'Save changes' : 'Create policy'}
          </Button>
        </footer>
      </form>
    </Modal>
  );
}
