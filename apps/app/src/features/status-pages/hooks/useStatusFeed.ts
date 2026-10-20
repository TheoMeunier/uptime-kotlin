import { useQuery } from '@tanstack/react-query';
import { ApiError } from '@/api/api-error.ts';
import statusPageService from '@/features/status-pages/services/status-page-service.ts';

const STATUS_FEED_REFRESH_MS = 120_000;

export default function useStatusFeed(slug?: string) {
	return useQuery({
		queryKey: ['status-feed', slug ?? null],
		queryFn: () => statusPageService.getStatusFeed(slug),
		refetchInterval: STATUS_FEED_REFRESH_MS,
		retry: (failureCount, error) => !(error instanceof ApiError && error.status === 404) && failureCount < 3,
		meta: { silentError: true },
	});
}
