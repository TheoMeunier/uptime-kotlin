import { CircleAlert, Info, type LucideIcon, TriangleAlert, Wrench } from 'lucide-react';
import type { AnnouncementSeverity } from '@/features/announcements/schemas/announcement.schema.ts';

interface SeverityStyle {
	icon: LucideIcon;
	panel: string;
	tone: string;
	dot: string;
}

export const SEVERITY_STYLE: Record<AnnouncementSeverity, SeverityStyle> = {
	INFO: {
		icon: Info,
		panel: 'bg-muted text-foreground border-border',
		tone: 'text-foreground',
		dot: 'bg-muted-foreground',
	},
	DEGRADED: {
		icon: TriangleAlert,
		panel: 'bg-status-degraded-bg text-status-degraded-fg border-status-degraded/40',
		tone: 'text-status-degraded-fg',
		dot: 'bg-status-degraded',
	},
	OUTAGE: {
		icon: CircleAlert,
		panel: 'bg-status-down-bg text-status-down-fg border-status-down/40',
		tone: 'text-status-down-fg',
		dot: 'bg-status-down',
	},
	MAINTENANCE: {
		icon: Wrench,
		panel: 'bg-status-maintenance-bg text-status-maintenance-fg border-status-maintenance/40',
		tone: 'text-status-maintenance-fg',
		dot: 'bg-status-maintenance',
	},
};
