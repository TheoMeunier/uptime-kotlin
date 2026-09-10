import { z } from 'zod';

export const MaintenanceRecurrenceEnum = ['ONCE', 'DAILY', 'WEEKLY', 'MONTHLY'] as const;
export type MaintenanceRecurrence = (typeof MaintenanceRecurrenceEnum)[number];

export const MaintenanceOccurrenceSchema = z.object({
	id: z.uuid(),
	starts_at: z.string(),
	ends_at: z.string(),
	cancelled: z.boolean(),
});

export type MaintenanceOccurrence = z.infer<typeof MaintenanceOccurrenceSchema>;

export const ProbeMaintenanceSchema = z.object({
	id: z.uuid(),
	window_id: z.uuid(),
	title: z.string(),
	starts_at: z.string(),
	ends_at: z.string(),
});

export type ProbeMaintenance = z.infer<typeof ProbeMaintenanceSchema>;

const MaintenanceProbeSchema = z.object({
	id: z.uuid(),
	name: z.string(),
	description: z.string().nullable().optional(),
	url: z.string().nullable().optional(),
	status: z.string(),
});

export const MaintenanceListItemSchema = z.object({
	id: z.uuid(),
	title: z.string(),
	recurrence: z.enum(MaintenanceRecurrenceEnum),
	duration_seconds: z.number(),
	timezone: z.string(),
	active: z.boolean(),
	is_public: z.boolean(),
	probe_count: z.number(),
	current_occurrence: MaintenanceOccurrenceSchema.nullable().optional(),
	next_occurrence: MaintenanceOccurrenceSchema.nullable().optional(),
});

export type MaintenanceListItem = z.infer<typeof MaintenanceListItemSchema>;

export const MaintenanceListSchema = z.array(MaintenanceListItemSchema);

export const MaintenanceDetailSchema = z.object({
	id: z.uuid(),
	title: z.string(),
	description: z.string().nullable(),
	starts_at: z.string(),
	duration_seconds: z.number(),
	recurrence: z.enum(MaintenanceRecurrenceEnum),
	recurrence_until: z.string().nullable(),
	timezone: z.string(),
	active: z.boolean(),
	is_public: z.boolean(),
	probes: z.array(MaintenanceProbeSchema),
	probe_ids: z.array(z.uuid()),
	current_occurrence: MaintenanceOccurrenceSchema.nullable().optional(),
	next_occurrence: MaintenanceOccurrenceSchema.nullable().optional(),
	upcoming_occurrences: z.array(MaintenanceOccurrenceSchema).default([]),
	created_at: z.string(),
	updated_at: z.string(),
});

export type MaintenanceDetail = z.infer<typeof MaintenanceDetailSchema>;

export const CreatedMaintenanceSchema = z.object({ id: z.uuid() });
