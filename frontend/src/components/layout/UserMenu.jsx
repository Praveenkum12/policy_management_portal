import { useEffect, useRef, useState } from 'react';
import { initials } from '../../utils/format';
import { ChevronDownIcon } from '../common';

const MENU_ITEMS = ['Profile', 'Settings', 'Sign out'];

export default function UserMenu({ name }) {
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  useEffect(() => {
    if (!open) return undefined;
    const onPointerDown = (event) => {
      if (!rootRef.current.contains(event.target)) setOpen(false);
    };
    const onKeyDown = (event) => event.key === 'Escape' && setOpen(false);
    document.addEventListener('pointerdown', onPointerDown);
    document.addEventListener('keydown', onKeyDown);
    return () => {
      document.removeEventListener('pointerdown', onPointerDown);
      document.removeEventListener('keydown', onKeyDown);
    };
  }, [open]);

  return (
    <div className="user-menu" ref={rootRef}>
      <button
        type="button"
        className="user-chip"
        aria-haspopup="menu"
        aria-expanded={open}
        onClick={() => setOpen((current) => !current)}
      >
        <span className="user-avatar">{initials(name)}</span>
        <span className="user-name">{name}</span>
        <ChevronDownIcon size={18} className={open ? 'chevron-open' : undefined} />
      </button>

      {open && (
        <div className="menu" role="menu">
          {MENU_ITEMS.map((item) => (
            <button key={item} type="button" className="menu-item" role="menuitem" disabled>
              {item}
              <span className="menu-soon">Soon</span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
