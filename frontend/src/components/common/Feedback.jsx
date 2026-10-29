import { Button } from './Button';
import { AlertIcon, CheckCircleIcon, InboxIcon, RefreshIcon } from './Icons';

export function ErrorBanner({ title, message, onRetry }) {
  return (
    <div className="banner banner-error" role="alert">
      <AlertIcon />
      <div className="banner-text">
        {title && <strong>{title}</strong>}
        <span>{message}</span>
      </div>
      {onRetry && (
        <Button variant="ghost" size="sm" icon={<RefreshIcon size={16} />} onClick={onRetry}>
          Retry
        </Button>
      )}
    </div>
  );
}

export function EmptyState({ title, description, action }) {
  return (
    <div className="empty">
      <div className="empty-icon">
        <InboxIcon size={28} />
      </div>
      <h2>{title}</h2>
      <p>{description}</p>
      {action}
    </div>
  );
}

export function SkeletonRows({ rows = 6, columns = 7 }) {
  return Array.from({ length: rows }, (_, row) => (
    <tr key={row} className="skeleton-row" aria-hidden="true">
      {Array.from({ length: columns }, (_, column) => (
        <td key={column}>
          <span className="skeleton" />
        </td>
      ))}
    </tr>
  ));
}

export function Toasts({ toasts, onDismiss }) {
  return (
    <div className="toasts" aria-live="polite">
      {toasts.map((toast) => (
        <div key={toast.id} className={`toast toast-${toast.type}`} role={toast.type === 'error' ? 'alert' : 'status'}>
          <span className="toast-icon">
            {toast.type === 'error' ? <AlertIcon size={18} /> : <CheckCircleIcon size={18} />}
          </span>
          <div className="toast-body">
            <strong className="toast-title">{toast.title}</strong>
            {toast.message && <span className="toast-message">{toast.message}</span>}
          </div>
          <button type="button" className="icon-btn toast-close" aria-label="Dismiss notification" onClick={() => onDismiss(toast.id)}>
            ×
          </button>
          <span className="toast-progress" style={{ animationDuration: `${toast.duration}ms` }} />
        </div>
      ))}
    </div>
  );
}
