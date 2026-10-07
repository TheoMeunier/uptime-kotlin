import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import statusPageService, { type StatusPagePayload } from '@/features/status-pages/services/status-page-service.ts';

export function useStatusPages() {
	return useQuery({
		queryKey: ['status-pages'],
		queryFn: () => statusPageService.getStatusPages(),
	});
}

export function useStatusPage(statusPageId?: string) {
	return useQuery({
		queryKey: ['status-page', statusPageId],
		queryFn: () => statusPageService.getStatusPage(statusPageId!),
		enabled: Boolean(statusPageId),
		refetchOnWindowFocus: false,
	});
}

async function invalidateStatusPages(client: ReturnType<typeof useQueryClient>) {
	await Promise.all([
		client.invalidateQueries({ queryKey: ['status-pages'] }),
		client.invalidateQueries({ queryKey: ['status-page'] }),
		client.invalidateQueries({ queryKey: ['status-page-public'] }),
	]);
}

export function useSaveStatusPage(statusPageId?: string) {
	const { t } = useTranslation();
	const client = useQueryClient();

	return useMutation({
		mutationFn: (payload: StatusPagePayload) =>
			statusPageId
				? statusPageService.updateStatusPage(statusPageId, payload)
				: statusPageService.storeStatusPage(payload),
		onSuccess: async (_, payload) => {
			await invalidateStatusPages(client);
			toast.success(
				t(statusPageId ? 'status_pages.alerts.updated' : 'status_pages.alerts.created', { title: payload.title })
			);
		},
	});
}

export function useDeleteStatusPage() {
	const { t } = useTranslation();
	const client = useQueryClient();

	return useMutation({
		mutationFn: (statusPageId: string) => statusPageService.deleteStatusPage(statusPageId),
		onSuccess: async () => {
			await invalidateStatusPages(client);
			toast.success(t('status_pages.alerts.removed'));
		},
	});
}
