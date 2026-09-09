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
