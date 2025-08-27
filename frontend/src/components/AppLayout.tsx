// filepath: c:\Users\pjs\IdeaProjects\Solebid\frontend\src\components\AppLayout.tsx
import { Outlet } from 'react-router-dom';
import AppHeader from './AppHeader';

function AppLayout() {
  return (
    <div className="min-h-screen bg-white">
      <AppHeader />
      <main className="mx-auto w-full max-w-7xl px-10">
        <Outlet />
      </main>
    </div>
  );
}

export default AppLayout;
