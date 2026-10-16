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
export type IncidentPeriod = (typeof IncidentPeriods)[number];

const PERIOD_HOURS: Record<IncidentPeriod, number> = {
	'24h': 24,
	'7d': 24 * 7,
	'30d': 24 * 30,
	'90d': 24 * 90,
	'365d': 24 * 365,
};

export function periodStart(period: IncidentPeriod, now: number = Date.now()): string {
	return new Date(now - PERIOD_HOURS[period] * 3_600_000).toISOString();
}
