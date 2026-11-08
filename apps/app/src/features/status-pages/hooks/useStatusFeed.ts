import { useQuery } from '@tanstack/react-query';
import { ApiError } from '@/api/api-error.ts';
import statusPageService from '@/features/status-pages/services/status-page-service.ts';
import { STATUS_QUERY_TIMING } from '@/features/status-pages/lib/status-refresh.ts';

export default function useStatusFeed(slug?: string, enabled = true) {
	return useQuery({
		queryKey: ['status-feed', slug ?? null],
		queryFn: () => statusPageService.getStatusFeed(slug),
		enabled,
		...STATUS_QUERY_TIMING,
		retry: (failureCount, error) => !(error instanceof ApiError && error.status === 404) && failureCount < 3,
		meta: { silentError: true },
	});
}
