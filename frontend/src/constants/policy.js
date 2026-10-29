export const POLICY_TYPES = ['Life', 'Health', 'Motor', 'Travel'];

export const POLICY_STATUSES = ['Active', 'Pending', 'Cancelled'];

export const STATUS_TONES = {
  Active: 'success',
  Pending: 'warning',
  Cancelled: 'danger',
};

export const PAGE_SIZES = [5, 10, 20, 50];

export const POLICY_NUMBER_PREFIX = 'POL-';

export const POLICY_COLUMNS = [
  { key: 'policyNumber', label: 'Policy No.', sortable: true },
  { key: 'customerName', label: 'Customer', sortable: true },
  { key: 'policyType', label: 'Type', sortable: true },
  { key: 'premiumAmount', label: 'Premium', sortable: true, align: 'right' },
  { key: 'status', label: 'Status', sortable: true },
  { key: 'startDate', label: 'Start date', sortable: true },
  { key: 'actions', label: 'Actions', sortable: false, align: 'right' },
];
