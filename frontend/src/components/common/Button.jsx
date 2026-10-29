export function Button({
  variant = 'secondary',
  size = 'md',
  icon,
  loading = false,
  disabled = false,
  children,
  className = '',
  ...props
}) {
  const classes = ['btn', `btn-${variant}`, size === 'sm' && 'btn-sm', className].filter(Boolean).join(' ');
  return (
    <button type="button" className={classes} disabled={loading || disabled} {...props}>
      {loading ? <span className="spinner" aria-hidden="true" /> : icon}
      {children}
    </button>
  );
}

export function IconButton({ tone = 'primary', label, children, ...props }) {
  return (
    <button type="button" className={`icon-btn icon-btn-${tone}`} aria-label={label} title={label} {...props}>
      {children}
    </button>
  );
}
