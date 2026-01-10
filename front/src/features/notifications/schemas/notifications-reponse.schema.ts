import { z } from 'zod';

const notificationsItems = z.object({
	id: z.uuid(),
	name: z.string().min(1, 'Le nom ne peut pas être vide'),
});

export const apiNotificationListingResponseSchema = z.array(notificationsItems);

export type NotificationItem = z.infer<typeof notificationsItems>;
export type NotificationListingApi = z.infer<typeof apiNotificationListingResponseSchema>;
