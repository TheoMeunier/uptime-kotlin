import { useQuery } from '@tanstack/react-query';
import settingsService from '@/features/settings/services/settingsService.ts';

export const LOG_RETENTION_QUERY_KEY = ['settings', 'retention'] as const;

export default function useLogRetentionSettings() {
	const { data, isLoading, isError, refetch } = useQuery({
		queryKey: LOG_RETENTION_QUERY_KEY,
		queryFn: async () => {
			return settingsService.getLogRetention();
		},
	});

	return { data, isLoading, isError, refetch };
}
