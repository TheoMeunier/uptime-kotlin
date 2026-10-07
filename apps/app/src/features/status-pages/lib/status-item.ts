import type { ProbeStatusShowResponse } from '@/features/probes/schemas/probe-response.schema.ts';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import { uptimeState } from '@/lib/status.ts';

export type StatusItem = ProbeStatusShowResponse[number];

export interface StatusSection {
	name?: string | null;
	items: StatusItem[];
}

export function uptimeTone(value: number) {
	const state = uptimeState(value);

	if (state === 'down') return 'text-status-down-fg';
	if (state === 'degraded') return 'text-status-degraded-fg';

	return 'text-foreground';
}

/* Colour only when something needs a look: a healthy service stays neutral (see lib/status.ts). */
export function attentionAccent(item: StatusItem): string | null {
	if (item.maintenance) return 'bg-status-maintenance';
	if (item.probe.status === ProbeStatusEnum.FAILURE) return 'bg-status-down';
	if (item.probe.status === ProbeStatusEnum.WARNING) return 'bg-status-degraded';

	return null;
}

export function isDown(item: StatusItem) {
	return !item.maintenance && item.probe.status === ProbeStatusEnum.FAILURE;
}

/* The https:// scheme tells a visitor nothing and eats half the column. */
export function displayTarget(url: string) {
	return url.replace(/^https?:\/\//, '').replace(/\/$/, '');
}

export interface Verdict {
	kind: 'down' | 'degraded' | 'operational';
	count: number;
	total: number;
}

/* Probes under maintenance are left out of the verdict: planned work is not an incident. */
export function summarize(items: StatusItem[]) {
	const watched = items.filter((item) => !item.maintenance);
	const total = watched.length;
	const down = watched.filter((item) => item.probe.status === ProbeStatusEnum.FAILURE).length;
	const degraded = watched.filter((item) => item.probe.status === ProbeStatusEnum.WARNING).length;

	const verdict: Verdict =
		down > 0
			? { kind: 'down', count: down, total }
			: degraded > 0
				? { kind: 'degraded', count: degraded, total }
				: { kind: 'operational', count: total, total };

	return { verdict, maintenanceCount: items.length - total };
}
