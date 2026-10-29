import { POLICY_COLUMNS } from '../../constants/policy';
import { formatCurrency, formatDate, initials } from '../../utils/format';
import {
  ArrowDownIcon,
  ArrowUpDownIcon,
  ArrowUpIcon,
  Button,
  CalendarIcon,
  CarSolidIcon,
  EditIcon,
  HeartSolidIcon,
  IconButton,
  PlaneSolidIcon,
  ShieldSolidIcon,
  SkeletonRows,
  TrashIcon,
} from '../common';
import StatusBadge from './StatusBadge';

const TYPE_ICONS = {
  Life: ShieldSolidIcon,
  Health: HeartSolidIcon,
  Motor: CarSolidIcon,
  Travel: PlaneSolidIcon,
};

function SortHeader({ column, sort, direction, onSort }) {
  const sorted = sort === column.key;
  const Indicator = !sorted ? ArrowUpDownIcon : direction === 'asc' ? ArrowUpIcon : ArrowDownIcon;
  return (
    <button type="button" className={`sort-btn ${sorted ? 'is-sorted' : ''}`} onClick={() => onSort(column.key)}>
      {column.label}
      <span className="sort-indicator">
        <Indicator size={14} />
      </span>
    </button>
  );
}

function TypeTag({ type }) {
  const Icon = TYPE_ICONS[type];
  return (
    <span className="type-tag">
      {Icon && <Icon size={20} className={`type-icon type-${type.toLowerCase()}`} />}
      {type}
    </span>
  );
}

export default function PolicyTable({ policies, loading, refreshing, sort, direction, onSort, onEdit, onDelete }) {
  return (
    <div className={`table-wrap ${refreshing ? 'is-refreshing' : ''}`} aria-busy={loading}>
      <table className="table">
        <thead>
          <tr>
            {POLICY_COLUMNS.map((column) => (
              <th
                key={column.key}
                scope="col"
                className={column.align === 'right' ? 'align-right' : undefined}
                aria-sort={
                  column.sortable && sort === column.key ? (direction === 'asc' ? 'ascending' : 'descending') : undefined
                }
              >
                {column.sortable ? (
                  <SortHeader column={column} sort={sort} direction={direction} onSort={onSort} />
                ) : (
                  column.label
                )}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading && !policies ? (
            <SkeletonRows rows={6} columns={POLICY_COLUMNS.length} />
          ) : (
            policies.map((policy) => (
              <tr key={policy.id}>
                <td className="cell-policy-number cell-primary" data-label="Policy No.">
                  {policy.policyNumber}
                </td>
                <td className="cell-primary" data-label="Customer">
                  <div className="cell-customer">
                    <span className="avatar">{initials(policy.customerName)}</span>
                    {policy.customerName}
                  </div>
                </td>
                <td data-label="Type">
                  <TypeTag type={policy.policyType} />
                </td>
                <td className="align-right cell-money" data-label="Premium">
                  {formatCurrency(policy.premiumAmount)}
                </td>
                <td data-label="Status">
                  <StatusBadge status={policy.status} />
                </td>
                <td className="cell-date" data-label="Start date">
                  <span className="date-tag">
                    <CalendarIcon size={18} />
                    {formatDate(policy.startDate)}
                  </span>
                </td>
                <td className="align-right cell-actions" data-label="Actions">
                  <div className="row-actions">
                    <span className="icon-actions">
                      <IconButton filled label={`Edit ${policy.policyNumber}`} onClick={() => onEdit(policy)}>
                        <EditIcon size={18} />
                      </IconButton>
                      <IconButton filled tone="danger" label={`Delete ${policy.policyNumber}`} onClick={() => onDelete(policy)}>
                        <TrashIcon size={18} />
                      </IconButton>
                    </span>
                    <span className="button-actions">
                      <Button size="sm" icon={<EditIcon size={16} />} onClick={() => onEdit(policy)}>
                        Edit
                      </Button>
                      <Button size="sm" variant="danger" icon={<TrashIcon size={16} />} onClick={() => onDelete(policy)}>
                        Delete
                      </Button>
                    </span>
                  </div>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
