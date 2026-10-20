import { z } from 'zod';
import { UptimePeriodEnum } from '@/features/status-pages/lib/uptime-period.ts';

export const UptimeBucketSchema = z.object({
	starts_at: z.string(),
	ends_at: z.string(),
	total: z.number(),
	successes: z.number(),
	failures: z.number(),
	warnings: z.number(),
	maintenance: z.number(),
});

export type UptimeBucket = z.infer<typeof UptimeBucketSchema>;

export const StatusUptimeSchema = z.object({
	period: z.enum(UptimePeriodEnum),
	timezone: z.string(),
	probes: z.array(
		z.object({
			probe_id: z.uuid(),
			uptime_percent: z.number().nullable(),
			buckets: z.array(UptimeBucketSchema),
		})
	),
});

export type StatusUptime = z.infer<typeof StatusUptimeSchema>;
export type ProbeUptimeBar = StatusUptime['probes'][number];
