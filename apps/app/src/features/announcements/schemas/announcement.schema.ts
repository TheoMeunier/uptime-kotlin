import { z } from 'zod';

export const AnnouncementSeverityEnum = ['INFO', 'DEGRADED', 'OUTAGE', 'MAINTENANCE'] as const;
export type AnnouncementSeverity = (typeof AnnouncementSeverityEnum)[number];

export const AnnouncementStateEnum = ['SCHEDULED', 'ACTIVE', 'ENDED'] as const;
export type AnnouncementState = (typeof AnnouncementStateEnum)[number];

export const PublicAnnouncementSchema = z.object({
	id: z.uuid(),
	title: z.string(),
	body: z.string().nullable(),
	severity: z.enum(AnnouncementSeverityEnum),
	starts_at: z.string(),
	ends_at: z.string().nullable(),
	updated_at: z.string(),
});

export type PublicAnnouncement = z.infer<typeof PublicAnnouncementSchema>;

export const AnnouncementSchema = PublicAnnouncementSchema.extend({
	state: z.enum(AnnouncementStateEnum),
	show_on_global: z.boolean(),
	pages: z.array(z.object({ id: z.uuid(), slug: z.string(), title: z.string() })),
	created_at: z.string(),
});

export type Announcement = z.infer<typeof AnnouncementSchema>;

export const AnnouncementListSchema = z.array(AnnouncementSchema);

export const CreatedAnnouncementSchema = z.object({ id: z.uuid() });
