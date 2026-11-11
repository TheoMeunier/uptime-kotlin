export const ROLES = ['admin', 'viewer'] as const;

export type Role = (typeof ROLES)[number];

export type Permission =
	| 'probes.create'
	| 'probes.update'
	| 'probes.delete'
	| 'probes.toggle'
	| 'probes.check'
	| 'probes.purge_logs'
	| 'incidents.acknowledge'
	| 'incidents.annotate'
	| 'maintenances.manage'
	| 'announcements.manage'
	| 'status_pages.manage'
	| 'notifications.manage'
	| 'retention.manage'
	| 'users.manage';

const ROLE_PERMISSIONS: Record<Role, '*' | readonly Permission[]> = {
	admin: '*',
	viewer: [],
};

export function isRole(value: unknown): value is Role {
	return typeof value === 'string' && (ROLES as readonly string[]).includes(value);
}

export function roleCan(role: Role | null, permission: Permission): boolean {
	if (!role) return false;
	const granted = ROLE_PERMISSIONS[role];
	return granted === '*' || granted.includes(permission);
}
