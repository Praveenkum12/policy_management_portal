import { useState } from 'react';
import { createPolicy, deletePolicy, updatePolicy } from './api/policyApi';
import { Button, ConfirmDialog, EmptyState, ErrorBanner, Pagination, PlusIcon, Toasts } from './components/common';
import PolicyFilters from './components/policy/PolicyFilters';
import PolicyFormModal from './components/policy/PolicyFormModal';
import PolicyTable from './components/policy/PolicyTable';
import { PAGE_SIZES } from './constants/policy';
import { usePolicies } from './hooks/usePolicies';
import { useToasts } from './hooks/useToasts';

export default function App() {
  const policies = usePolicies();
  const { toasts, push, dismiss } = useToasts();
  const [formState, setFormState] = useState(null);
  const [deleting, setDeleting] = useState(null);

  const { query, data, loading, error } = policies;
  const hasFilters = Boolean(query.search || query.status || policies.searchInput.trim());
  const isEmpty = data && data.content.length === 0;

  async function savePolicy(body) {
    const editing = formState.policy;
    if (editing) {
      const updated = await updatePolicy(editing.id, body);
      push({ title: 'Policy updated successfully', message: `${updated.policyNumber} for ${updated.customerName} has been saved.` });
    } else {
      const created = await createPolicy(body);
      push({ title: 'Policy created successfully', message: `${created.policyNumber} for ${created.customerName} has been added.` });
    }
    setFormState(null);
    policies.reload();
  }

  async function confirmDelete() {
    await deletePolicy(deleting.id);
    push({ title: 'Policy deleted successfully', message: `${deleting.policyNumber} for ${deleting.customerName} has been removed.` });
    setDeleting(null);
    policies.reload();
  }

  const openCreate = () => setFormState({ policy: null });

  return (
    <>
      <main className="page">
        <div className="page-heading">
          <div>
            <h1>Policies</h1>
            <p>View, search and manage insurance policies.</p>
          </div>
          <Button variant="primary" icon={<PlusIcon size={18} />} onClick={openCreate}>
            New policy
          </Button>
        </div>

        <section className="card" aria-label="Policy list">
          <PolicyFilters
            search={policies.searchInput}
            status={query.status}
            totalElements={data?.totalElements}
            hasFilters={hasFilters}
            onSearchChange={policies.setSearchInput}
            onStatusChange={policies.setStatus}
            onClear={policies.clearFilters}
          />

          {error && !data && (
            <div className="card-section">
              <ErrorBanner title="Could not load policies" message={error.message} onRetry={policies.reload} />
            </div>
          )}

          {error && data && (
            <div className="card-section">
              <ErrorBanner title="Could not refresh policies" message={error.message} onRetry={policies.reload} />
            </div>
          )}

          {isEmpty && !error ? (
            <EmptyState
              title={hasFilters ? 'No policies match your search' : 'No policies yet'}
              description={
                hasFilters
                  ? 'Try a different name or policy number, or clear the filters.'
                  : 'Create your first policy to see it listed here.'
              }
              action={
                hasFilters ? (
                  <Button onClick={policies.clearFilters}>Clear filters</Button>
                ) : (
                  <Button variant="primary" icon={<PlusIcon size={18} />} onClick={openCreate}>
                    New policy
                  </Button>
                )
              }
            />
          ) : (
            (data || (loading && !error)) && (
              <PolicyTable
                policies={data?.content}
                loading={loading}
                refreshing={loading && Boolean(data)}
                sort={query.sort}
                direction={query.direction}
                onSort={policies.toggleSort}
                onEdit={(policy) => setFormState({ policy })}
                onDelete={setDeleting}
              />
            )
          )}

          {data && !isEmpty && (
            <Pagination
              page={data.page}
              totalPages={data.totalPages}
              totalElements={data.totalElements}
              count={data.content.length}
              limit={data.limit}
              pageSizes={PAGE_SIZES}
              onPageChange={policies.setPage}
              onLimitChange={policies.setLimit}
            />
          )}
        </section>
      </main>

      {formState && (
        <PolicyFormModal policy={formState.policy} onClose={() => setFormState(null)} onSubmit={savePolicy} />
      )}

      {deleting && (
        <ConfirmDialog title="Delete policy" onCancel={() => setDeleting(null)} onConfirm={confirmDelete}>
          Are you sure you want to delete policy <strong>{deleting.policyNumber}</strong> for{' '}
          <strong>{deleting.customerName}</strong>? This action cannot be undone.
        </ConfirmDialog>
      )}

      <Toasts toasts={toasts} onDismiss={dismiss} />
    </>
  );
}
