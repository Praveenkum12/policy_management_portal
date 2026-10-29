import { Link } from 'react-router-dom';
import { EmptyState } from '../components/common';

export default function NotFoundPage() {
  return (
    <section className="card">
      <EmptyState
        title="Page not found"
        description="The page you are looking for does not exist or has moved."
        action={
          <Link to="/policies" className="btn btn-primary">
            Go to Policies
          </Link>
        }
      />
    </section>
  );
}
