import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import settingsService from '@/features/settings/services/settingsService.ts';
import { LOG_RETENTION_QUERY_KEY } from '@/features/settings/hooks/useLogRetentionSettings.ts';

export default function useUpdateLogRetention() {
	const { t } = useTranslation();
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: async (days: number | null) => settingsService.updateLogRetention(days),
		onSuccess: async (saved) => {
			client.setQueryData(LOG_RETENTION_QUERY_KEY, saved);
			await client.invalidateQueries({ queryKey: ['probe-update'] });
			toast.success(t('retention.alerts.updated'));
		},
	});

	return { update: mutation.mutate, isLoading: mutation.isPending };
}
