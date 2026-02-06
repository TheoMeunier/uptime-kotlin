import { Navigate, Outlet } from 'react-router';
import useSetup from '@/features/setup/hooks/useSetupApp.ts';
import LoaderPage from '@/features/setup/components/loader-page.tsx';

export function SetupAppProvider() {
	const { isSetupComplete, isLoading } = useSetup();

	if (isLoading) {
		return <LoaderPage />;
	}

	if (!isSetupComplete) {
		return <Navigate replace to="/setup" />;
	}

	return <Outlet />;
}
