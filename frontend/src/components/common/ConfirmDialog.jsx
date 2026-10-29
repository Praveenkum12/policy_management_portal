import { useEffect, useRef, useState } from 'react';
import { Button } from './Button';
import { ErrorBanner } from './Feedback';
import { TrashIcon } from './Icons';
import Modal from './Modal';

export default function ConfirmDialog({ title, children, confirmLabel = 'Delete', busyLabel = 'Deleting…', onCancel, onConfirm }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  const cancelRef = useRef(null);

  useEffect(() => {
    cancelRef.current?.focus();
  }, []);

  async function confirm() {
    setBusy(true);
    setError(null);
    try {
      await onConfirm();
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  }

  return (
    <Modal title={title} onClose={onCancel} size="sm" dismissible={!busy} alert>
      <div className="confirm">
        <div className="confirm-icon">
          <TrashIcon size={26} />
        </div>
        <h2 className="confirm-title">{title}</h2>
        <p className="confirm-text">{children}</p>
        {error && <ErrorBanner message={error} />}
        <div className="confirm-actions">
          <button ref={cancelRef} type="button" className="btn btn-secondary" onClick={onCancel} disabled={busy}>
            Cancel
          </button>
          <Button variant="danger" loading={busy} onClick={confirm}>
            {busy ? busyLabel : confirmLabel}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
