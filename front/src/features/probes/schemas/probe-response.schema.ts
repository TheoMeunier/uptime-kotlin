import { z } from 'zod';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import ProbeMonitorsSchema from '@/features/probes/schemas/probe-monitor.schema.ts';

const ProbeListSidebarSchema = z.array(
	z.object({
		id: z.uuid(),
		name: z.string().min(3).max(255),
		status: z.enum(ProbeStatusEnum),
		description: z.string().nullable(),
	})
);

export type ProbeListItem = z.infer<typeof ProbeListSidebarSchema>[number];

export default ProbeListSidebarSchema;

export const ProbeResponseSchema = z.object({
	id: z.uuid(),
	name: z.string().min(3).max(255),
	status: z.enum(ProbeStatusEnum),
	description: z.string().nullable(),
	last_check: z.date(),
	interval: z.number(),
	retry: z.number(),
	timeout: z.number(),
	enabled: z.boolean(),
	notification_certificate: z.boolean(),
	ignore_certificate_errors: z.boolean(),
	http_code_allowed: z.array(z.number()),
	tcp_port: z.number(),
	dns_server: z.string(),
	dns_port: z.string(),
	url: z.string(),
	ping_heartbeat_interval: z.number(),
	ping_max_packet: z.number(),
	ping_size: z.number(),
	ping_delay: z.number(),
	ping_numeric_output: z.boolean(),
	created_at: z.date(),
	updated_at: z.date(),
});

export const ProbeShowSchema = z.object({
	probe: ProbeResponseSchema,
	monitors: ProbeMonitorsSchema,
});

export type ProbeShow = z.infer<typeof ProbeShowSchema>;
