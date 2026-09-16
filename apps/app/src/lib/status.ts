import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';

/**
 * Visual weight of a value. `neutral` is the default on purpose: colour is spent on what needs
 * attention, never on decoration. A "Down" tile showing 0 must stay neutral.
 */
export type MetricState = 'neutral' | 'up' | 'degraded' | 'down';

interface StatusTokens {
	/** Solid fill, for dots and chart bars. */
	solid: string;
	/** Badge background. */
	bg: string;
	/** Text on top of `bg`, and standalone coloured values. */
	fg: string;
	/** i18n key of the human label. */
	labelKey: string;
}

export const statusTokens: Record<ProbeStatusEnum, StatusTokens> = {
	[ProbeStatusEnum.SUCCESS]: {
		solid: 'bg-status-up',
		bg: 'bg-status-up-bg',
		fg: 'text-status-up-fg',
		labelKey: 'status.healthy',
	},
	[ProbeStatusEnum.WARNING]: {
		solid: 'bg-status-degraded',
		bg: 'bg-status-degraded-bg',
		fg: 'text-status-degraded-fg',
		labelKey: 'status.degraded',
	},
	[ProbeStatusEnum.FAILURE]: {
		solid: 'bg-status-down',
		bg: 'bg-status-down-bg',
		fg: 'text-status-down-fg',
		labelKey: 'status.unhealthy',
	},
	[ProbeStatusEnum.PAUSE]: {
		solid: 'bg-status-paused',
		bg: 'bg-status-paused-bg',
		fg: 'text-status-paused-fg',
		labelKey: 'status.paused',
	},
};

export const unknownStatusTokens: StatusTokens = statusTokens[ProbeStatusEnum.FAILURE];

export function getStatusTokens(status: ProbeStatusEnum | string): StatusTokens {
	return statusTokens[status as ProbeStatusEnum] ?? unknownStatusTokens;
}

/**
 * Uptime thresholds. Deliberately forgiving compared to a hosted SLA: on a self-hosted instance a
 * single restart drops a 30-day figure below 99.9%, and an alert that fires for a healthy service
 * teaches people to ignore alerts.
 */
export function uptimeState(percent: number): MetricState {
	if (percent < 95) return 'down';
	if (percent < 99) return 'degraded';
	return 'neutral';
}

/** A count of failing things: neutral at zero, critical otherwise. */
export function failureCountState(count: number | undefined): MetricState {
	return count && count > 0 ? 'down' : 'neutral';
}

export type AttentionSeverity = 'none' | 'degraded' | 'down';

export interface AttentionSummary {
	severity: AttentionSeverity;
	/** How many probes sit in the worst state currently present — the figure the tab badge shows. */
	count: number;
}

/**
 * What the tab has to shout about. Only the worst state present is counted, so `(2)` next to a red
 * dot always means "two of them are down" and never a mixed total nobody can act on. A paused probe
 * is a decision, not an incident: it never counts.
 */
export function summarizeAttention(statuses: Array<ProbeStatusEnum | string> | undefined): AttentionSummary {
	let down = 0;
	let degraded = 0;

	for (const status of statuses ?? []) {
		if (status === ProbeStatusEnum.FAILURE) down += 1;
		else if (status === ProbeStatusEnum.WARNING) degraded += 1;
	}

	if (down > 0) return { severity: 'down', count: down };
	if (degraded > 0) return { severity: 'degraded', count: degraded };

	return { severity: 'none', count: 0 };
}
