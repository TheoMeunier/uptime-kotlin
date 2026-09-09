import { z } from 'zod';

const summary = z.object({
	total_monitors: z.number(),
	total_monitors_success: z.number(),
	total_monitors_failures: z.number(),
	avg_uptime_percent: z.number(),
});

const metrics_last_days = z.object({
	avg_response_time_ms: z.number(),
	count_incidents24h: z.number(),
	count_checks24h: z.number(),
});

const down_probes = z.object({
	id: z.uuid(),
	name: z.string(),
	down_duration: z.string(),
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

const recent_event = z.object({
	probe_id: z.uuid(),
	probe_name: z.string(),
	status: z.string(),
	message: z.string(),
	run_at: z.coerce.date(),
});

const DashboardStatsSchema = z.object({
	summary: summary,
	metrics_last_days: metrics_last_days,
	down_probes: z.array(down_probes),
	latency_spark_line: z.array(spark_line_point),
	incident_bar: z.array(incident_bar),
	check_spark_line: z.array(spark_line_point),
	recent_events: z.array(recent_event).default([]),
});

export type DashboardStats = z.infer<typeof DashboardStatsSchema>;
export default DashboardStatsSchema;
