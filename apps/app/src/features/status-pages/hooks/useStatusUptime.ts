import { useQuery } from '@tanstack/react-query';
import statusPageService from '@/features/status-pages/services/status-page-service.ts';
import { STATUS_QUERY_TIMING } from '@/features/status-pages/lib/status-refresh.ts';
import { isAggregated, type UptimePeriod } from '@/features/status-pages/lib/uptime-period.ts';

const visitorTimezone = () => Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC';

export default function useStatusUptime(slug: string, period: UptimePeriod | undefined) {
	const timezone = visitorTimezone();

	return useQuery({
		queryKey: ['status-uptime', slug, period, timezone],
		queryFn: () => statusPageService.getStatusUptime(slug, timezone),
		enabled: Boolean(slug && period && isAggregated(period)),
		...STATUS_QUERY_TIMING,
		meta: { silentError: true },
	});
}
