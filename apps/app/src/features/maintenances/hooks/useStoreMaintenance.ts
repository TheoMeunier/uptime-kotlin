import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import maintenanceService from '@/features/maintenances/services/maintenance-service.ts';
import type { StoreMaintenanceSchema } from '@/features/maintenances/hooks/useMaintenanceForm.ts';

/** Everything a maintenance change can move: the list, a probe's windows, and the status page. */
export async function invalidateMaintenance(
	client: ReturnType<typeof useQueryClient>,
	probeId?: string
): Promise<void> {
	await Promise.all([
		client.invalidateQueries({ queryKey: ['maintenances'] }),
		client.invalidateQueries({ queryKey: ['probes-status'] }),
		probeId
			? client.invalidateQueries({ queryKey: ['probe-maintenances', probeId] })
			: client.invalidateQueries({ queryKey: ['probe-maintenances'] }),
		client.invalidateQueries({ queryKey: ['probe'] }),
	]);
}

export default function useStoreMaintenance(maintenanceId?: string) {
	const { t } = useTranslation();
	const client = useQueryClient();
	const [openDialogue, setOpenDialogue] = useState(false);

	const mutation = useMutation({
		mutationFn: async (data: StoreMaintenanceSchema) => {
			return maintenanceId
				? maintenanceService.updateMaintenance(maintenanceId, data)
				: maintenanceService.storeMaintenance(data);
		},
		onSuccess: async (_, variables) => {
			await invalidateMaintenance(client);
			setOpenDialogue(false);
			toast.success(
				t(maintenanceId ? 'maintenances.alerts.updated' : 'maintenances.alerts.created', { title: variables.title })
			);
		},
	});

	return {
		openDialogue,
		setOpenDialogue,
		onSubmit: (data: StoreMaintenanceSchema) => mutation.mutate(data),
		isLoading: mutation.isPending,
	};
}
