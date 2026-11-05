import { Navigate, Outlet } from 'react-router';
import { useRole } from '@/features/auth/lib/role.ts';

export function AdminRoute() {
	const { isAdmin } = useRole();

	return isAdmin ? <Outlet /> : <Navigate replace to="/dashboard" />;
}
