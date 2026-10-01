import { useMutation } from '@tanstack/react-query';
import settingsService from '@/features/settings/services/settingsService.ts';

export function usePreviewLogRetention() {
	const mutation = useMutation({
		mutationFn: async (days: number | null) => settingsService.previewLogRetention(days),
	});

	return { preview: mutation.mutate, isLoading: mutation.isPending };
}

export function usePreviewProbeLogRetention(probeId?: string) {
	const mutation = useMutation({
		mutationFn: async (days: number | null) => settingsService.previewProbeLogRetention(probeId!, days),
	});

	return { preview: mutation.mutateAsync, isLoading: mutation.isPending };
}
