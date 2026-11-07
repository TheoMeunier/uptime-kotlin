import { useTranslation } from 'react-i18next';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { StoreNotificationSchema } from '@/features/notifications/hooks/useNotificationForm.ts';
import notificationService from '@/features/notifications/services/notification-service.ts';
import { toast } from 'sonner';
import { useState } from 'react';

interface UseStoreNotificationOptions {
	onCreated?: (notificationId: string) => void;
}

export default function useStoreNotification({ onCreated }: UseStoreNotificationOptions = {}) {
	const { t } = useTranslation();
	const queryClient = useQueryClient();
	const [openDialogue, setOpenDialogue] = useState(false);

	const mutation = useMutation({
		mutationFn: async (data: StoreNotificationSchema) => {
			return notificationService.storeNotification(data);
		},
		onSuccess: (created, v) => {
			onCreated?.(created.id);
			queryClient.invalidateQueries({ queryKey: ['notifications'] }).then(() => {
				setOpenDialogue(false);
				toast.success(t('notifications.alerts.create', { data: v.name }));
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
