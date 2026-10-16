import { z } from 'zod';
import { IncidentClosedReasonEnum } from '@/features/incidents/schemas/incident.schema.ts';

const summary = z.object({
	total_monitors: z.number(),
	total_monitors_success: z.number(),
	total_monitors_failures: z.number(),
	avg_uptime_percent: z.number(),
});

const metrics_last_days = z.object({
	avg_response_time_ms: z.number(),
	count_checks24h: z.number(),
});

const incident_bar = z.object({
	hour: z.coerce.date(),
	up_count: z.number(),
	down_count: z.number(),
});

const spark_line_point = z.object({
	bucket: z.coerce.date(),
	value: z.number(),
});

/** The opening (at the first failed check) or the closing of an incident. */
const recent_event = z.object({
	incident_id: z.uuid(),
	probe_id: z.uuid(),
	probe_name: z.string(),
	type: z.enum(['OPENED', 'CLOSED']),
	at: z.coerce.date(),
	message: z.string(),
	ongoing: z.boolean(),
	duration_seconds: z.number().nullable().optional(),
	closed_reason: z.enum(IncidentClosedReasonEnum).nullable().optional(),
});

const DashboardStatsSchema = z.object({
	summary: summary,
	metrics_last_days: metrics_last_days,
	latency_spark_line: z.array(spark_line_point),
	incident_bar: z.array(incident_bar),
	check_spark_line: z.array(spark_line_point),
	recent_events: z.array(recent_event).default([]),
});

export type DashboardStats = z.infer<typeof DashboardStatsSchema>;
export default DashboardStatsSchema;
