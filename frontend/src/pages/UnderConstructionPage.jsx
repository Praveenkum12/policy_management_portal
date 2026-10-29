import { Link } from 'react-router-dom';
import { ArrowLeftIcon } from '../components/common';

function HardHat() {
  return (
    <svg className="construction-art" viewBox="0 0 160 120" role="img" aria-label="Hard hat">
      <ellipse cx="80" cy="104" rx="56" ry="7" fill="var(--color-border)" />
      <path d="M32 84C32 50 52 30 80 30s48 20 48 54z" fill="var(--color-warning)" />
      <path d="M68 28h24v56H68z" fill="var(--color-text)" opacity="0.18" />
      <path d="M32 84C32 50 52 30 80 30" fill="none" stroke="#fff" strokeOpacity="0.35" strokeWidth="4" strokeLinecap="round" />
      <rect x="20" y="82" width="120" height="14" rx="7" fill="var(--color-warning)" />
      <rect x="20" y="90" width="120" height="6" rx="3" fill="var(--color-text)" opacity="0.18" />
    </svg>
  );
}

export default function UnderConstructionPage({ title, description }) {
  return (
    <section className="construction card" aria-labelledby="construction-title">
      <div className="construction-tape" aria-hidden="true" />
      <div className="construction-body">
        <HardHat />
        <span className="construction-badge">Under construction</span>
        <h1 id="construction-title">{title} module is on the way</h1>
        <p className="construction-text">{description}</p>
        <p className="construction-text">We&apos;re still building this section. Everything about policies is available now.</p>

        <Link to="/policies" className="btn btn-primary">
          <ArrowLeftIcon size={18} />
          Back to Policies
        </Link>
      </div>
      <div className="construction-tape" aria-hidden="true" />
    </section>
  );
}
