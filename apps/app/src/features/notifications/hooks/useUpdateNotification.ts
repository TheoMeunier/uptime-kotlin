import { useTranslation } from 'react-i18next';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { StoreNotificationSchema } from '@/features/notifications/hooks/useNotificationForm.ts';
import notificationService from '@/features/notifications/services/notification-service.ts';
import { toast } from 'sonner';
import { useState } from 'react';

export default function useUpdateNotification(notificationId: string) {
	const { t } = useTranslation();
	const queryClient = useQueryClient();
	const [openDialogue, setOpenDialogue] = useState(false);

	const mutation = useMutation({
		mutationFn: async (data: StoreNotificationSchema) => {
			return notificationService.updateNotification(notificationId, data);
		},
		onSuccess: (_, v) => {
			Promise.all([
				queryClient.invalidateQueries({ queryKey: ['notifications'] }),
				queryClient.invalidateQueries({ queryKey: ['notifications-setting'] }),
			]).then(() => {
				setOpenDialogue(false);
				toast.success(t('notifications.alerts.update', { data: v.name }));
			});
		},
	});

	const onSubmit = async (data: StoreNotificationSchema) => {
		mutation.mutate(data);
	};

	return {
		openDialogue,
		setOpenDialogue,
		onSubmit,
		isLoading: mutation.isPending,
	};
}
