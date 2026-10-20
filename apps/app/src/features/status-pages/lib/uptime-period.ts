import type { UptimeBucket } from '@/features/status-pages/schemas/status-uptime.schema.ts';

export const UptimePeriodEnum = ['H1', 'D1', 'D7', 'D30', 'D60', 'D90'] as const;
export type UptimePeriod = (typeof UptimePeriodEnum)[number];

interface PeriodSpec {
	bars: number;
	requiredDays: number;
	unit: 'hours' | 'days';
	span: number;
}

export const UPTIME_PERIODS: Record<UptimePeriod, PeriodSpec> = {
	H1: { bars: 30, requiredDays: 1, unit: 'hours', span: 1 },
	D1: { bars: 24, requiredDays: 1, unit: 'hours', span: 24 },
	D7: { bars: 42, requiredDays: 7, unit: 'days', span: 7 },
	D30: { bars: 30, requiredDays: 30, unit: 'days', span: 30 },
	D60: { bars: 60, requiredDays: 60, unit: 'days', span: 60 },
	D90: { bars: 90, requiredDays: 90, unit: 'days', span: 90 },
};

export const isAggregated = (period: UptimePeriod) => period !== 'H1';

export function shortestRetentionDays(
	probes: { log_retention_days?: number | null }[],
	defaultDays: number | null | undefined
): number | null {
	const days = probes
		.map((probe) => probe.log_retention_days ?? defaultDays ?? null)
		.filter((value): value is number => value != null && value > 0);

	return days.length > 0 ? Math.min(...days) : null;
}

export const isPeriodAllowed = (period: UptimePeriod, retentionDays: number | null) =>
	retentionDays == null || UPTIME_PERIODS[period].requiredDays <= retentionDays;

export type BucketState = 'up' | 'degraded' | 'down' | 'maintenance' | 'nodata';

/** Below this availability a bucket is an outage; any failed or degraded check above it makes it degraded. */
export const DOWN_BELOW_PERCENT = 99;

/** Fill of a bar and colour of its label, per state. */
export const BUCKET_TONE: Record<BucketState, { solid: string; text: string }> = {
	up: { solid: 'bg-status-up', text: 'text-status-up-fg' },
	degraded: { solid: 'bg-status-degraded', text: 'text-status-degraded-fg' },
	down: { solid: 'bg-status-down', text: 'text-status-down-fg' },
	maintenance: { solid: 'bg-status-maintenance', text: 'text-status-maintenance-fg' },
	nodata: { solid: 'bg-status-nodata', text: 'text-muted-foreground' },
};

export function bucketUptime(bucket: UptimeBucket): number | null {
	return bucket.total > 0 ? (bucket.successes * 100) / bucket.total : null;
}

export function bucketState(bucket: UptimeBucket): BucketState {
	if (bucket.total === 0) return bucket.maintenance > 0 ? 'maintenance' : 'nodata';
	if (bucket.failures === 0 && bucket.warnings === 0) return 'up';

	return (bucketUptime(bucket) ?? 100) < DOWN_BELOW_PERCENT ? 'down' : 'degraded';
}

export function formatUptime(percent: number): string {
	const fixed = percent.toFixed(2);
	return percent < 100 && fixed === '100.00' ? '99.99%' : `${fixed}%`;
}
