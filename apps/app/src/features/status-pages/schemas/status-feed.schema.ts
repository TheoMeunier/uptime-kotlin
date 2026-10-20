import { z } from 'zod';
import { PublicAnnouncementSchema } from '@/features/announcements/schemas/announcement.schema.ts';

export const StatusFeedSchema = z.object({
	announcements: z.array(PublicAnnouncementSchema),
});

export type StatusFeed = z.infer<typeof StatusFeedSchema>;
