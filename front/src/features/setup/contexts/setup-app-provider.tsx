import { Navigate, Outlet } from 'react-router';
import useSetup from '@/features/setup/hooks/useSetupApp.ts';

export function SetupAppProvider() {
	const { isSetupComplete, isLoading } = useSetup();

	if (isLoading) {
		return <div>Loading...</div>;
	}

	if (!isSetupComplete) {
		return <Navigate replace to="/setup" />;
	}

	return <Outlet />;
}
