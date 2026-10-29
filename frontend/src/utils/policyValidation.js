import { POLICY_NUMBER_PREFIX } from '../constants/policy';

export const EMPTY_POLICY_FORM = {
  policyNumber: '',
  customerName: '',
  policyType: '',
  premiumAmount: '',
  status: '',
  startDate: '',
};

export function toFormValues(policy) {
  return {
    policyNumber: policy.policyNumber.replace(new RegExp(`^${POLICY_NUMBER_PREFIX}`, 'i'), ''),
    customerName: policy.customerName,
    policyType: policy.policyType,
    premiumAmount: String(policy.premiumAmount),
    status: policy.status,
    startDate: policy.startDate,
  };
}

export function toRequestBody(values) {
  return {
    policyNumber: values.policyNumber.trim(),
    customerName: values.customerName.trim(),
    policyType: values.policyType,
    premiumAmount: Number(values.premiumAmount),
    status: values.status,
    startDate: values.startDate,
  };
}

export function validatePolicy(values) {
  const errors = {};

  if (!values.policyNumber.trim()) errors.policyNumber = 'Policy number is required';
  else if (!/^\d+$/.test(values.policyNumber.trim())) errors.policyNumber = 'Policy number must contain only digits';
  else if (values.policyNumber.trim().length > 10) errors.policyNumber = 'Policy number must be at most 10 digits';

  if (!values.customerName.trim()) errors.customerName = 'Customer name is required';
  else if (values.customerName.trim().length > 120) errors.customerName = 'Customer name must be at most 120 characters';

  if (!values.policyType) errors.policyType = 'Select a policy type';

  const premium = Number(values.premiumAmount);
  if (values.premiumAmount === '') errors.premiumAmount = 'Premium amount is required';
  else if (!Number.isFinite(premium) || premium <= 0) errors.premiumAmount = 'Premium amount must be greater than zero';
  else if (!/^\d{1,10}(\.\d{1,2})?$/.test(values.premiumAmount)) {
    errors.premiumAmount = 'Use up to 10 digits and 2 decimals';
  }

  if (!values.status) errors.status = 'Select a status';

  if (!values.startDate) errors.startDate = 'Start date is required';
  else if (Number.isNaN(Date.parse(values.startDate))) errors.startDate = 'Enter a valid date';

  return errors;
}
