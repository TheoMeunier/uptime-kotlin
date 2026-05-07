import { data, useNavigate } from 'react-router';
import { type SubmitHandler } from 'react-hook-form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import probeService from '@/features/probes/services/probeService.ts';
import { toast } from 'sonner';
import { useTranslation } from 'react-i18next';
import type { StoreProbeSchema } from '@/features/probes/hooks/useProbeForm.ts';

export function useStoreMonitor() {
	const { t } = useTranslation();
	const navigate = useNavigate();
	const queryClient = useQueryClient();

	const mutation = useMutation({
		mutationFn: async (data: StoreProbeSchema) => {
			return probeService.storeProbe(data);
		},
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['probes'] }).then(() => {
				toast.success(t('monitors.alerts.create', { data: data.name }));
				navigate('/dashboard');
			});
		},
	});

	const onsubmit: SubmitHandler<StoreProbeSchema> = async (data: StoreProbeSchema) => {
		mutation.mutate(data);
	};

	return {
		onsubmit,
		isLoading: mutation.isPending,
	};
}
