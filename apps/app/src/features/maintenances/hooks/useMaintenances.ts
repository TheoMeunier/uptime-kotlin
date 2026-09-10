import { useQuery } from '@tanstack/react-query';
import maintenanceService from '@/features/maintenances/services/maintenance-service.ts';

export default function useMaintenances() {
	const { data, isLoading, isError, refetch } = useQuery({
		queryKey: ['maintenances'],
		queryFn: async () => {
			return maintenanceService.getMaintenances();
		},
		refetchInterval: 60000,
	});

	return { data, isLoading, isError, refetch };
}

export function useProbeMaintenances(probeId: string) {
	const { data, isLoading } = useQuery({
		queryKey: ['probe-maintenances', probeId],
		queryFn: async () => {
			return maintenanceService.getProbeMaintenances(probeId);
		},
	});

	return { data, isLoading };
}
