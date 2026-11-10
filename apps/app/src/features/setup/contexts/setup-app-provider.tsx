import { Navigate, Outlet, useLocation } from 'react-router';
import useSetup from '@/features/setup/hooks/useSetupApp.ts';
import LoaderPage from '@/features/setup/components/loader-page.tsx';
import StatusBoardFallback from '@/features/status-pages/components/board/status-board-fallback.tsx';
import { isStatusBoardPath } from '@/features/status-pages/lib/status-board-path.ts';

export function SetupAppProvider() {
	const { isSetupComplete, isLoading } = useSetup();
	const { pathname } = useLocation();

	if (isLoading) {
		return isStatusBoardPath(pathname) ? <StatusBoardFallback /> : <LoaderPage />;
	}

	if (!isSetupComplete) {
		return <Navigate replace to="/setup" />;
	}

	return <Outlet />;
}
