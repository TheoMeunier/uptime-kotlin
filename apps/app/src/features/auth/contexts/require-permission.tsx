import { Navigate, Outlet } from 'react-router';
import { usePermissions } from '@/features/auth/lib/role.ts';
import type { Permission } from '@/features/auth/lib/permissions.ts';

export function RequirePermission({ permission }: { permission: Permission }) {
	const { can } = usePermissions();

	return can(permission) ? <Outlet /> : <Navigate replace to="/dashboard" />;
}
