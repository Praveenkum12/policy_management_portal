import { NavLink } from 'react-router-dom';
import { NAV_ITEMS } from '../../constants/navigation';
import { useTheme } from '../../hooks/useTheme';
import { BarChartIcon, DocumentIcon, MoonIcon, ShieldIcon, SunIcon, UsersIcon } from '../common';
import UserMenu from './UserMenu';

const NAV_ICONS = { document: DocumentIcon, users: UsersIcon, chart: BarChartIcon };

export default function AppHeader() {
  const { theme, toggleTheme } = useTheme();
  const nextTheme = theme === 'dark' ? 'light' : 'dark';

  return (
    <header className="app-header">
      <div className="app-header-inner">
        <NavLink to="/policies" className="brand" aria-label="PolicyPortal home">
          <span className="brand-mark">
            <ShieldIcon size={20} />
          </span>
          <span className="brand-name">PolicyPortal</span>
        </NavLink>

        <nav className="nav" aria-label="Main">
          {NAV_ITEMS.map(({ to, label, icon }) => {
            const Icon = NAV_ICONS[icon];
            return (
              <NavLink key={to} to={to} className="nav-tab">
                <Icon size={19} />
                <span>{label}</span>
              </NavLink>
            );
          })}
        </nav>

        <div className="header-actions">
          <button
            type="button"
            className="theme-toggle"
            onClick={toggleTheme}
            aria-label={`Switch to ${nextTheme} mode`}
            title={`Switch to ${nextTheme} mode`}
          >
            {theme === 'dark' ? <SunIcon size={20} /> : <MoonIcon size={20} />}
          </button>
          <UserMenu name="Praveen Kumar" />
        </div>
      </div>
    </header>
  );
}
