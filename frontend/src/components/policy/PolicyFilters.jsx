import { POLICY_STATUSES, POLICY_TYPES } from '../../constants/policy';
import { Button, CloseIcon, FilterIcon, IconButton, RefreshIcon, SearchIcon, SelectInput, ShieldIcon, TextInput } from '../common';

export default function PolicyFilters({
  search,
  status,
  policyType,
  hasFilters,
  onSearchChange,
  onStatusChange,
  onTypeChange,
  onReset,
}) {
  return (
    <div className="filters" role="search">
      <div className="filters-search">
        <TextInput
          type="search"
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
          placeholder="Search by customer name or policy number…"
          aria-label="Search policies"
          icon={<SearchIcon size={20} />}
          trailing={
            search && (
              <IconButton label="Clear search" onClick={() => onSearchChange('')}>
                <CloseIcon size={16} />
              </IconButton>
            )
          }
        />
      </div>
      <SelectInput
        value={policyType}
        onChange={(event) => onTypeChange(event.target.value)}
        options={POLICY_TYPES}
        placeholder="All types"
        aria-label="Filter by policy type"
        icon={<ShieldIcon size={19} />}
      />
      <SelectInput
        value={status}
        onChange={(event) => onStatusChange(event.target.value)}
        options={POLICY_STATUSES}
        placeholder="All statuses"
        aria-label="Filter by status"
        icon={<FilterIcon size={18} />}
      />
      <Button icon={<RefreshIcon size={18} />} onClick={onReset} disabled={!hasFilters}>
        Reset
      </Button>
    </div>
  );
}
