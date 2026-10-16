import { z } from 'zod';

export const IncidentClosedReasonEnum = ['RECOVERY', 'PROBE_DISABLED'] as const;
export type IncidentClosedReason = (typeof IncidentClosedReasonEnum)[number];

export const IncidentSchema = z.object({
	id: z.uuid(),
	probe_id: z.uuid(),
	probe_name: z.string(),
	started_at: z.coerce.date(),
	confirmed_at: z.coerce.date(),
	ended_at: z.coerce.date().nullable().optional(),
	duration_seconds: z.number().nullable().optional(),
	opening_message: z.string(),
	closed_reason: z.enum(IncidentClosedReasonEnum).nullable().optional(),
	acknowledged_at: z.coerce.date().nullable().optional(),
	note: z.string().nullable().optional(),
	note_updated_at: z.coerce.date().nullable().optional(),
});

export type Incident = z.infer<typeof IncidentSchema>;

export const IncidentPageSchema = z.object({
	items: z.array(IncidentSchema),
	total: z.number(),
	page: z.number(),
	size: z.number(),
});

export type IncidentPage = z.infer<typeof IncidentPageSchema>;

export const IncidentSummarySchema = z.object({
	from: z.coerce.date(),
	to: z.coerce.date(),
	count: z.number(),
	ongoing: z.number(),
	total_downtime_seconds: z.number(),
	mttr_seconds: z.number().nullable().optional(),
	longest: IncidentSchema.nullable().optional(),
});

export type IncidentSummary = z.infer<typeof IncidentSummarySchema>;

export const IncidentSpanSchema = z.object({
	id: z.uuid(),
	started_at: z.coerce.date(),
	ended_at: z.coerce.date().nullable().optional(),
});

export type IncidentSpan = z.infer<typeof IncidentSpanSchema>;

export const IncidentPeriods = ['24h', '7d', '30d', '90d', '365d'] as const;
export type RollingIncidentPeriod = (typeof IncidentPeriods)[number];

/** A calendar month, `2026-09`, bounded in the browser's time zone. */
export type MonthPeriod = `${number}-${string}`;
export type IncidentPeriod = RollingIncidentPeriod | MonthPeriod;

const PERIOD_HOURS: Record<RollingIncidentPeriod, number> = {
	'24h': 24,
	'7d': 24 * 7,
	'30d': 24 * 30,
	'90d': 24 * 90,
	'365d': 24 * 365,
};

const MONTH_PATTERN = /^(\d{4})-(0[1-9]|1[0-2])$/;

export function isMonthPeriod(value: string | null | undefined): value is MonthPeriod {
	return value != null && MONTH_PATTERN.test(value);
}

export function parseIncidentPeriod(value: string | null | undefined): IncidentPeriod | undefined {
	if (IncidentPeriods.includes(value as RollingIncidentPeriod)) return value as RollingIncidentPeriod;
	return isMonthPeriod(value) ? value : undefined;
}

function monthPeriodOf(date: Date): MonthPeriod {
	return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
}

/** The current month first, then the ones before it. */
export function recentMonths(count: number, now: Date = new Date()): MonthPeriod[] {
	return Array.from({ length: count }, (_, index) =>
		monthPeriodOf(new Date(now.getFullYear(), now.getMonth() - index, 1))
	);
}

export function monthStart(period: MonthPeriod): Date {
	const [year, month] = period.split('-').map(Number);
	return new Date(year, month - 1, 1);
}

/**
 * A rolling period ends now, so it has no `to`: the API takes the time of the request. A month runs from its
 * first midnight to the next month's, in the browser's time zone.
 */
export function periodRange(period: IncidentPeriod, now: number = Date.now()): { from: string; to?: string } {
	if (!isMonthPeriod(period)) {
		return { from: new Date(now - PERIOD_HOURS[period] * 3_600_000).toISOString() };
	}

	const start = monthStart(period);
	const end = new Date(start.getFullYear(), start.getMonth() + 1, 1);
	return { from: start.toISOString(), to: end.toISOString() };
}
