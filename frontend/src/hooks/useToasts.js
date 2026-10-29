import { useCallback, useState } from 'react';

const DURATIONS = { success: 4000, error: 6000 };

export function useToasts() {
  const [toasts, setToasts] = useState([]);

  const dismiss = useCallback((id) => setToasts((current) => current.filter((toast) => toast.id !== id)), []);

  const push = useCallback(
    ({ type = 'success', title, message }) => {
      const id = crypto.randomUUID();
      const duration = DURATIONS[type] ?? DURATIONS.success;
      setToasts((current) => [...current, { id, type, title, message, duration }]);
      setTimeout(() => dismiss(id), duration);
    },
    [dismiss],
  );

  return { toasts, push, dismiss };
}
