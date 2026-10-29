import { useEffect, useRef } from 'react';
import { CloseIcon } from './Icons';

const FOCUSABLE = 'button:not([disabled]), input:not([disabled]), select:not([disabled]), [href]';

export default function Modal({ title, onClose, children, size = 'md', dismissible = true, alert = false }) {
  const panelRef = useRef(null);
  const onCloseRef = useRef(onClose);
  onCloseRef.current = onClose;

  useEffect(() => {
    const previouslyFocused = document.activeElement;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    const onKeyDown = (event) => {
      if (event.key === 'Escape' && dismissible) onCloseRef.current();
      if (event.key !== 'Tab') return;
      const focusable = panelRef.current.querySelectorAll(FOCUSABLE);
      if (focusable.length === 0) return;
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    };
    document.addEventListener('keydown', onKeyDown);

    return () => {
      document.removeEventListener('keydown', onKeyDown);
      document.body.style.overflow = previousOverflow;
      previouslyFocused?.focus?.();
    };
  }, [dismissible]);

  return (
    <div className="overlay" onMouseDown={(event) => event.target === event.currentTarget && dismissible && onClose()}>
      <div ref={panelRef} className={`modal modal-${size}`} role={alert ? 'alertdialog' : 'dialog'} aria-modal="true" aria-label={title}>
        {!alert && (
          <header className="modal-header">
            <h2>{title}</h2>
            <button type="button" className="icon-btn" aria-label="Close" onClick={onClose} disabled={!dismissible}>
              <CloseIcon />
            </button>
          </header>
        )}
        {children}
      </div>
    </div>
  );
}
