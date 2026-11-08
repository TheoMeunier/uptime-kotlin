import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import settingsService from '@/features/settings/services/settingsService.ts';

export const DEFAULT_STATUS_PAGE_QUERY_KEY = ['settings', 'status-page'] as const;

export function useDefaultStatusPageSettings() {
	return useQuery({
		queryKey: DEFAULT_STATUS_PAGE_QUERY_KEY,
		queryFn: () => settingsService.getDefaultStatusPage(),
	});
}

export function useUpdateDefaultStatusPage() {
	const { t } = useTranslation();
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: (isPrivate: boolean) => settingsService.updateDefaultStatusPage(isPrivate),
		onSuccess: (saved) => {
			client.setQueryData(DEFAULT_STATUS_PAGE_QUERY_KEY, saved);
			toast.success(t(saved.is_private ? 'status_pages.global.alerts.private' : 'status_pages.global.alerts.public'));
		},
	});

	return { update: mutation.mutate, isLoading: mutation.isPending };
}
