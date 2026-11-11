import type { ReactNode } from 'react';
import { usePermissions } from '@/features/auth/lib/role.ts';
import type { Permission } from '@/features/auth/lib/permissions.ts';

export default function Can({
	permission,
	children,
	fallback = null,
}: {
	permission: Permission;
	children: ReactNode;
	fallback?: ReactNode;
}) {
	const { can } = usePermissions();

	return can(permission) ? children : fallback;
}
