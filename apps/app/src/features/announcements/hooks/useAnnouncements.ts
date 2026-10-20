import { type QueryClient, useQuery } from '@tanstack/react-query';
import announcementService from '@/features/announcements/services/announcement-service.ts';

export async function invalidateAnnouncements(client: QueryClient): Promise<void> {
	await Promise.all([
		client.invalidateQueries({ queryKey: ['announcements'] }),
		client.invalidateQueries({ queryKey: ['announcement'] }),
		client.invalidateQueries({ queryKey: ['status-feed'] }),
	]);
}

export default function useAnnouncements() {
	return useQuery({
		queryKey: ['announcements'],
		queryFn: () => announcementService.getAnnouncements(),
		refetchInterval: 60_000,
	});
}

export function useAnnouncement(announcementId: string, enabled: boolean) {
	return useQuery({
		queryKey: ['announcement', announcementId],
		queryFn: () => announcementService.getAnnouncement(announcementId),
		enabled,
		refetchOnWindowFocus: false,
	});
}
