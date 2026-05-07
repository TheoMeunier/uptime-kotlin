import { z } from 'zod';
import NotificationTypeEnum from '@/features/notifications/enums/notification-type-enum.ts';

const notificationsItems = z.object({
	id: z.uuid(),
	name: z.string().min(1, 'Name is required'),
	is_default: z.boolean(),
});

export const apiNotificationListingResponseSchema = z.array(notificationsItems);

export type NotificationItem = z.infer<typeof notificationsItems>;
export type NotificationListingApi = z.infer<typeof apiNotificationListingResponseSchema>;

const notificationDetail = z.object({
	id: z.uuid(),
	name: z.string(),
	notification_type: z.enum(NotificationTypeEnum),
	content: z.any().nullable(),
	is_default: z.boolean(),
	created_at: z.coerce.date(),
});

export const apiNotificationDetailResponseSchema = z.array(notificationDetail);

export type NotificationDetail = z.infer<typeof notificationDetail>;
export type NotificationDetailApi = z.infer<typeof apiNotificationDetailResponseSchema>;
