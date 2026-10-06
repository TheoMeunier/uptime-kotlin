export type TourSide = 'top' | 'right' | 'bottom' | 'left';

export interface TourStep {
	id: string;
	target?: string;
	side?: TourSide;
}

export const TOUR_STEPS: readonly TourStep[] = [
	{ id: 'welcome' },
	{ id: 'new_monitor', target: 'new-monitor', side: 'right' },
	{ id: 'dashboard', target: 'dashboard', side: 'right' },
	{ id: 'monitors', target: 'monitors', side: 'right' },
	{ id: 'maintenances', target: 'maintenances', side: 'right' },
	{ id: 'status_page', target: 'status-page', side: 'right' },
	{ id: 'header', target: 'header-tools', side: 'bottom' },
	{ id: 'settings', target: 'user-menu', side: 'right' },
	{ id: 'done' },
];
