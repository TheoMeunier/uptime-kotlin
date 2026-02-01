import { z } from 'zod';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import ProbeMonitorsSchema from '@/features/probes/schemas/probe-monitor.schema.ts';
import ProbeProtocol from '@/features/probes/enums/probe-enum.ts';

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
	interval: z.number(),
	timeout: z.number(),
	retry: z.number(),
	interval_retry: z.number(),
	enabled: z.boolean(),
	protocol: z.enum(ProbeProtocol),
	description: z.string().nullable(),
	last_run: z.string().nullable(),
	status: z.enum(ProbeStatusEnum),
	content: z.any().nullable(),
	created_at: z.string(),
	updated_at: z.string(),
});

export const ProbeShowSchema = z.object({
	probe: ProbeResponseSchema,
	monitors: ProbeMonitorsSchema,
});

export type ProbeShow = z.infer<typeof ProbeShowSchema>;

const ProbeForStatusPageSchema = z.object({
	id: z.uuid(),
	name: z.string(),
	description: z.string().optional(),
	status: z.enum(ProbeStatusEnum),
});

export const ProbeStatusShowSchema = z.array(
	z.object({
		probe: ProbeForStatusPageSchema,
		monitors: ProbeMonitorsSchema,
	})
);

export type ProbeStatusShowResponse = z.infer<typeof ProbeStatusShowSchema>;

export const GetProbeUpdateResponseSchema = z.object({
	probe: ProbeResponseSchema,
	notifications: z.array(z.uuid()),
});

export type ProbeGetUpdateResponse = z.infer<typeof GetProbeUpdateResponseSchema>;
