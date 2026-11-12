import { Navigate, Outlet, useLocation } from 'react-router';
import authService from '@/features/auth/services/authServices.ts';
import { CHANGE_PASSWORD_PATH, changePasswordPath, loginPath } from '@/features/auth/lib/redirect.ts';

export function ProtectedRouteProvider() {
	const location = useLocation();
	const token = authService.getAccessToken();

	if (!token) {
		return <Navigate replace to={loginPath(location.pathname + location.search)} />;
	}

	const onChangePasswordPage = location.pathname === CHANGE_PASSWORD_PATH;
	const mustChangePassword = authService.mustChangePassword();

	if (mustChangePassword && !onChangePasswordPage) {
		return <Navigate replace to={changePasswordPath(location.pathname + location.search)} />;
	}

	if (!mustChangePassword && onChangePasswordPage) {
		return <Navigate replace to="/dashboard" />;
	}

	return (
		<>
			<Outlet />
		</>
	);
}
