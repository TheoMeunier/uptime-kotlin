import { useQuery } from '@tanstack/react-query';
import notificationService from '@/features/notifications/services/notification-service.ts';

export default function useNotificationsSetting() {
	const { data, isLoading } = useQuery({
		queryKey: ['notifications-setting'],
		queryFn: async () => {
			return notificationService.getNotificationsSettings();
		},
	});

	return {
		data: data,
		isLoading: isLoading,
	};
}
