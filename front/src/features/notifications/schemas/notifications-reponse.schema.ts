import { z } from 'zod';

const notificationsItems = z.object({
	id: z.uuid(),
	name: z.string().min(1, 'Name is required'),
	is_default: z.boolean(),
});

export const apiNotificationListingResponseSchema = z.array(notificationsItems);

export type NotificationItem = z.infer<typeof notificationsItems>;
export type NotificationListingApi = z.infer<typeof apiNotificationListingResponseSchema>;
