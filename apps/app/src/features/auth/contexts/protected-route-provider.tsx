import { Navigate, Outlet, useLocation } from 'react-router';
import authService from '@/features/auth/services/authServices.ts';
import { loginPath } from '@/features/auth/lib/redirect.ts';

export function ProtectedRouteProvider() {
	const location = useLocation();
	const token = authService.getAccessToken();

	if (!token) {
		return <Navigate replace to={loginPath(location.pathname + location.search)} />;
	}

	return (
		<>
			<Outlet />
		</>
	);
}
