import type { StoreNotificationSchema } from '@/features/notifications/hooks/useNotificationForm.ts';
import api from '@/api/kyClient.ts';
import {
	apiNotificationListingResponseSchema,
	type NotificationListingApi,
} from '@/features/notifications/schemas/notifications-reponse.schema.ts';

const notificationService = {
	async getNotifications(): Promise<NotificationListingApi> {
		const response = await api.get('notifications').json();
		return apiNotificationListingResponseSchema.parse(response);
	},

	async storeNotification(data: StoreNotificationSchema) {
		await api
			.post('notifications/new', {
				body: JSON.stringify(data),
			})
			.json();
	},
};

export default notificationService;
