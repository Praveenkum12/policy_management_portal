import { ChevronLeftIcon, ChevronRightIcon } from './Icons';

function pageWindow(current, total) {
  const pages = new Set([0, total - 1, current - 1, current, current + 1]);
  const sorted = [...pages].filter((p) => p >= 0 && p < total).sort((a, b) => a - b);
  return sorted.flatMap((p, i) => (i > 0 && p - sorted[i - 1] > 1 ? ['gap-' + p, p] : [p]));
}

export default function Pagination({ page, totalPages, totalElements, count, limit, pageSizes, onPageChange, onLimitChange }) {
  const from = totalElements === 0 ? 0 : page * limit + 1;
  const to = page * limit + count;

  return (
    <nav className="pagination" aria-label="Pagination">
      <span>
        Showing <strong>{from}</strong>–<strong>{to}</strong> of <strong>{totalElements}</strong> {totalElements === 1 ? 'policy' : 'policies'}
      </span>

      <label className="pagination-size">
        Rows per page
        <select value={limit} onChange={(event) => onLimitChange(Number(event.target.value))}>
          {pageSizes.map((size) => (
            <option key={size} value={size}>
              {size}
            </option>
          ))}
        </select>
      </label>

      <div className="pagination-pages">
        <button
          type="button"
          className="page-btn"
          aria-label="Previous page"
          disabled={page === 0}
          onClick={() => onPageChange(page - 1)}
        >
          <ChevronLeftIcon size={16} />
        </button>
        {pageWindow(page, totalPages).map((entry) =>
          typeof entry === 'string' ? (
            <span key={entry} className="page-gap">
              …
            </span>
          ) : (
            <button
              key={entry}
              type="button"
              className={`page-btn ${entry === page ? 'page-btn-active' : ''}`}
              aria-current={entry === page ? 'page' : undefined}
              onClick={() => onPageChange(entry)}
            >
              {entry + 1}
            </button>
          ),
        )}
        <button
          type="button"
          className="page-btn"
          aria-label="Next page"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
        >
          <ChevronRightIcon size={16} />
        </button>
      </div>
    </nav>
  );
}
