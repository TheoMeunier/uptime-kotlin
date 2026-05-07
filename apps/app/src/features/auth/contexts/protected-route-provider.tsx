import { Navigate, Outlet } from 'react-router';
import authService from '@/features/auth/services/authServices.ts';

export function ProtectedRouteProvider() {
	const token = authService.getAccessToken();

	if (!token) {
		return <Navigate replace to="/login" />;
	}

	return (
		<>
			<Outlet />
		</>
	);
}
