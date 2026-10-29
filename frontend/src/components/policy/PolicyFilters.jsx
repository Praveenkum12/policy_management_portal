import { POLICY_STATUSES } from '../../constants/policy';
import { Button, CloseIcon, IconButton, SearchIcon, SelectInput, TextInput } from '../common';

export default function PolicyFilters({ search, status, totalElements, hasFilters, onSearchChange, onStatusChange, onClear }) {
  return (
    <div className="filters" role="search">
      <div className="search-field">
        <TextInput
          type="search"
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
          placeholder="Search by customer name or policy number"
          aria-label="Search policies"
          icon={<SearchIcon size={18} />}
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
        value={status}
        onChange={(event) => onStatusChange(event.target.value)}
        options={POLICY_STATUSES}
        placeholder="All statuses"
        aria-label="Filter by status"
      />
      {hasFilters ? (
        <Button variant="ghost" onClick={onClear}>
          Clear filters
        </Button>
      ) : (
        <span />
      )}
      {totalElements !== undefined && (
        <span className="filters-count">
          {totalElements} {totalElements === 1 ? 'policy' : 'policies'}
          {hasFilters ? ' found' : ' in total'}
        </span>
      )}
    </div>
  );
}
