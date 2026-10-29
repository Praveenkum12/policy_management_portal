import { Navigate, Route, Routes } from 'react-router-dom';
import AppHeader from './components/layout/AppHeader';
import { UPCOMING_MODULES } from './constants/navigation';
import { usePageTitle } from './hooks/usePageTitle';
import NotFoundPage from './pages/NotFoundPage';
import PoliciesPage from './pages/PoliciesPage';
import UnderConstructionPage from './pages/UnderConstructionPage';

function UpcomingModule({ module }) {
  usePageTitle(module.title);
  return <UnderConstructionPage {...module} />;
}

export default function App() {
  return (
    <>
      <AppHeader />
      <main className="page">
        <Routes>
          <Route path="/" element={<Navigate to="/policies" replace />} />
          <Route path="/policies" element={<PoliciesPage />} />
          <Route path="/customers" element={<UpcomingModule module={UPCOMING_MODULES.customers} />} />
          <Route path="/reports" element={<UpcomingModule module={UPCOMING_MODULES.reports} />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </>
  );
}
