export type Crumb = {
	key: string | null;
	label?: string;
	to?: string;
	pending?: boolean;
};

const HOME: Crumb = { key: 'layout.sidebar.dashboard', to: '/dashboard' };

export function buildCrumbs(pathname: string, resolveProbe: (id: string) => Partial<Crumb>): Crumb[] {
	const segments = pathname.split('/').filter(Boolean);
	const [first, second, third] = segments;

	if (first === 'dashboard') return [{ key: 'layout.sidebar.dashboard' }];
	if (first === 'maintenances') return [HOME, { key: 'layout.sidebar.maintenances' }];
	if (first === 'profile') return [HOME, { key: 'layout.sidebar.settings' }];

	if (first === 'monitors') {
		if (second === 'new') return [HOME, { key: 'monitors.title.create' }];
		if (!second) return [HOME];

		const probe: Crumb = { key: null, ...resolveProbe(second), to: `/monitors/${second}` };

		if (third === 'edit') return [HOME, probe, { key: 'monitors.title.update' }];

		return [HOME, { ...probe, to: undefined }];
	}

	return [];
}
