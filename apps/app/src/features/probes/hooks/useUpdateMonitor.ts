import { useMutation, useQueryClient } from '@tanstack/react-query';
import probeService from '@/features/probes/services/probeService.ts';
import { toast } from 'sonner';
import { useNavigate } from 'react-router';
import type { SubmitHandler } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import type { StoreProbeSchema } from '@/features/probes/hooks/useProbeForm.ts';

export default function useUpdateMonitor(probeId: string) {
	const { t } = useTranslation();
	const navigate = useNavigate();
	const queryClient = useQueryClient();

	const mutation = useMutation({
		mutationFn: async (data: StoreProbeSchema) => {
			return probeService.updateProbe(probeId, data);
		},
		onSuccess: (_, v) => {
			queryClient.invalidateQueries({ queryKey: ['probes'] }).then(() => {
				toast.success(t('monitors.alerts.update', { data: v.name }));
				navigate(`/monitors/${probeId}`);
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
