import { z } from 'zod';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import { ProbeStatusShowSchema } from '@/features/probes/schemas/probe-response.schema.ts';

export const StatusPageLayoutEnum = ['GRID', 'LIST'] as const;
export type StatusPageLayoutValue = (typeof StatusPageLayoutEnum)[number];

export const StatusPageListSchema = z.array(
	z.object({
		id: z.uuid(),
		slug: z.string(),
		title: z.string(),
		description: z.string().nullable(),
		default_layout: z.enum(StatusPageLayoutEnum),
		group_count: z.number(),
		probe_count: z.number(),
		updated_at: z.string(),
	})
);

export type StatusPageListItem = z.infer<typeof StatusPageListSchema>[number];

export const StatusPageProbeSchema = z.object({
	id: z.uuid(),
	name: z.string(),
	description: z.string().nullable().optional(),
	url: z.string().nullable().optional(),
	status: z.enum(ProbeStatusEnum),
});

export type StatusPageProbe = z.infer<typeof StatusPageProbeSchema>;

export const StatusPageDetailSchema = z.object({
	id: z.uuid(),
	slug: z.string(),
	title: z.string(),
	description: z.string().nullable(),
	default_layout: z.enum(StatusPageLayoutEnum),
	groups: z.array(
		z.object({
			name: z.string().nullable(),
			probes: z.array(StatusPageProbeSchema),
		})
	),
	created_at: z.string(),
	updated_at: z.string(),
});

export type StatusPageDetail = z.infer<typeof StatusPageDetailSchema>;

export const PublicStatusPageSchema = z.object({
	slug: z.string(),
	title: z.string(),
	description: z.string().nullable(),
	default_layout: z.enum(StatusPageLayoutEnum),
	groups: z.array(
		z.object({
			name: z.string().nullable(),
			items: ProbeStatusShowSchema,
		})
	),
});

export type PublicStatusPage = z.infer<typeof PublicStatusPageSchema>;

export const CreatedStatusPageSchema = z.object({ id: z.uuid() });
