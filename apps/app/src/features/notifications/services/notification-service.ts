import type { StoreNotificationSchema } from '@/features/notifications/hooks/useNotificationForm.ts';
import api from '@/api/kyClient.ts';
import {
	apiNotificationDetailResponseSchema,
	apiNotificationListingResponseSchema,
	type NotificationDetailApi,
	type NotificationListingApi,
} from '@/features/notifications/schemas/notifications-reponse.schema.ts';

const notificationService = {
	async getNotifications(): Promise<NotificationListingApi> {
		const response = await api.get('notifications').json();
		return apiNotificationListingResponseSchema.parse(response);
	},

	async getNotificationsSettings(): Promise<NotificationDetailApi> {
		const response = await api.get('notifications/settings').json();
		return apiNotificationDetailResponseSchema.parse(response);
	},

	async storeNotification(data: StoreNotificationSchema) {
		await api
			.post('notifications/new', {
				body: JSON.stringify(data),
			})
			.json();
	},

	async updateNotification(notificationId: string, data: StoreNotificationSchema) {
		await api
			.post(`notifications/${notificationId}/update`, {
				body: JSON.stringify(data),
			})
			.json();
	},

	async deleteNotification(notificationId: string) {
		await api.post(`notifications/${notificationId}/remove`).json();
	},

	async testNotification(data: StoreNotificationSchema) {
		await api
			.post('notifications/testing', {
				body: JSON.stringify(data),
			})
			.json();
	},
};

export default notificationService;
