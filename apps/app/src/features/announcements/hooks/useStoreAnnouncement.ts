import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import announcementService from '@/features/announcements/services/announcement-service.ts';
import { invalidateAnnouncements } from '@/features/announcements/hooks/useAnnouncements.ts';
import type { AnnouncementFormValues } from '@/features/announcements/hooks/useAnnouncementForm.ts';

export default function useStoreAnnouncement(announcementId?: string) {
	const { t } = useTranslation();
	const client = useQueryClient();
	const [open, setOpen] = useState(false);

	const mutation = useMutation({
		mutationFn: (values: AnnouncementFormValues) =>
			announcementId
				? announcementService.updateAnnouncement(announcementId, values)
				: announcementService.storeAnnouncement(values),
		onSuccess: async (_, values) => {
			await invalidateAnnouncements(client);
			setOpen(false);
			toast.success(
				t(announcementId ? 'announcements.alerts.updated' : 'announcements.alerts.created', { title: values.title })
			);
		},
	});

	return {
		open,
		setOpen,
		onSubmit: (values: AnnouncementFormValues) => mutation.mutate(values),
		isLoading: mutation.isPending,
	};
}
