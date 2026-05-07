import { useMutation, useQueryClient } from '@tanstack/react-query';
import notificationService from '@/features/notifications/services/notification-service.ts';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';

export default function useDeleteNotification(notificationId: string) {
	const client = useQueryClient();
	const { t } = useTranslation();
	const form = useForm();
	const [openDialogue, setOpenDialogue] = useState(false);

	const mutation = useMutation({
		mutationFn: async () => {
			return notificationService.deleteNotification(notificationId);
		},
		onSuccess: () => {
			Promise.all([
				client.invalidateQueries({ queryKey: ['notifications'] }),
				client.invalidateQueries({ queryKey: ['notifications-setting'] }),
			]).then(() => {
				toast.success(t('notifications.alerts.remove'));
				setOpenDialogue(false);
			});
		},
	});

	const onSubmit = () => mutation.mutate();

	return {
		openDialogue,
		setOpenDialogue,
		onSubmit,
		form,
	};
}
