import { useTranslation } from 'react-i18next';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import maintenanceService from '@/features/maintenances/services/maintenance-service.ts';
import { invalidateMaintenance } from '@/features/maintenances/hooks/useStoreMaintenance.ts';

export function useDeleteMaintenance() {
	const { t } = useTranslation();
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: async (maintenanceId: string) => maintenanceService.deleteMaintenance(maintenanceId),
		onSuccess: async () => {
			await invalidateMaintenance(client);
			toast.success(t('maintenances.alerts.removed'));
		},
	});

	return { remove: mutation.mutate, isLoading: mutation.isPending };
}

export function useEndMaintenance(probeId?: string) {
	const { t } = useTranslation();
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: async (maintenanceId: string) => maintenanceService.endMaintenance(maintenanceId),
		onSuccess: async () => {
			await invalidateMaintenance(client, probeId);
			toast.success(t('maintenances.alerts.ended'));
		},
	});

	return { end: mutation.mutate, isLoading: mutation.isPending };
}

export function useCancelOccurrence() {
	const { t } = useTranslation();
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: async (occurrenceId: string) => maintenanceService.cancelOccurrence(occurrenceId),
		onSuccess: async () => {
			await invalidateMaintenance(client);
			toast.success(t('maintenances.alerts.occurrence_cancelled'));
		},
	});

	return { cancel: mutation.mutate, isLoading: mutation.isPending };
}

export function useStartAdHocMaintenance(probeId: string) {
	const { t } = useTranslation();
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: async ({ durationMinutes, title }: { durationMinutes: number; title?: string }) =>
			maintenanceService.startAdHoc(probeId, durationMinutes, title),
		onSuccess: async () => {
			await invalidateMaintenance(client, probeId);
			toast.success(t('maintenances.alerts.started'));
		},
	});

	return { start: mutation.mutate, isLoading: mutation.isPending };
}
