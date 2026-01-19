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

const DashboardStatsSchema = z.object({
	summary: summary,
	metrics_last_days: metrics_last_days,
	down_probes: z.array(down_probes),
});

export type DashboardStats = z.infer<typeof DashboardStatsSchema>;
export default DashboardStatsSchema;
