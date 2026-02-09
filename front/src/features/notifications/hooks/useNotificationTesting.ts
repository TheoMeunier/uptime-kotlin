import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import type { StoreNotificationSchema } from '@/features/notifications/hooks/useNotificationForm.ts';
import notificationService from '@/features/notifications/services/notification-service.ts';
import { useTranslation } from 'react-i18next';

export default function useNotificationTesting() {
	const { t } = useTranslation();

	const mutation = useMutation({
		mutationKey: ['notification-test'],
		mutationFn: async (data: StoreNotificationSchema) => {
			return notificationService.testNotification(data);
		},
		onSuccess: () => {
			toast.success(t('notifications.alerts.testing.success'));
		},
		onError: () => {
			toast.success(t('notifications.alerts.testing.error'));
		},
	});

	return {
		isTesting: mutation.isPending,
		testNotification: mutation.mutate,
	};
}
