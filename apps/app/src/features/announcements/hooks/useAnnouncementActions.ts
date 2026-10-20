import { useTranslation } from 'react-i18next';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import announcementService from '@/features/announcements/services/announcement-service.ts';
import { invalidateAnnouncements } from '@/features/announcements/hooks/useAnnouncements.ts';

export function useEndAnnouncement() {
	const { t } = useTranslation();
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: (announcementId: string) => announcementService.endAnnouncement(announcementId),
		onSuccess: () => toast.success(t('announcements.alerts.ended')),
		onSettled: () => invalidateAnnouncements(client),
	});

	return { end: mutation.mutate, isLoading: mutation.isPending };
}

export function useDeleteAnnouncement() {
	const { t } = useTranslation();
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: (announcementId: string) => announcementService.deleteAnnouncement(announcementId),
		onSuccess: () => toast.success(t('announcements.alerts.removed')),
		onSettled: () => invalidateAnnouncements(client),
	});

	return { remove: mutation.mutate, isLoading: mutation.isPending };
}
