import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import incidentService from '@/features/incidents/services/incident-service.ts';
import { type IncidentPeriod, periodStart } from '@/features/incidents/schemas/incident.schema.ts';

export const INCIDENTS_PAGE_SIZE = 20;
const ONGOING_LIMIT = 100;

export interface IncidentFilters {
	period: IncidentPeriod;
	probeId?: string;
}

export function useOngoingIncidents(probeId?: string) {
	const { data, isLoading, isError, refetch } = useQuery({
		queryKey: ['incidents', 'ongoing', probeId ?? null],
		queryFn: () => incidentService.getIncidents({ state: 'open', probeId, size: ONGOING_LIMIT }),
		refetchInterval: 30_000,
	});

	return { data, isLoading, isError, refetch };
}

export function useIncidentHistory({ period, probeId }: IncidentFilters, page: number) {
	const { data, isLoading, isError, isFetching, refetch } = useQuery({
		queryKey: ['incidents', 'history', period, probeId ?? null, page],
		queryFn: () =>
			incidentService.getIncidents({
				state: 'closed',
				probeId,
				from: periodStart(period),
				page,
				size: INCIDENTS_PAGE_SIZE,
			}),
		placeholderData: (previousData) => previousData,
		refetchInterval: 60_000,
	});

	return { data, isLoading, isError, isFetching, refetch };
}

export function useIncidentSummary({ period, probeId }: IncidentFilters) {
	const { data, isLoading, isError, refetch } = useQuery({
		queryKey: ['incidents', 'summary', period, probeId ?? null],
		queryFn: () => incidentService.getSummary({ probeId, from: periodStart(period) }),
		placeholderData: (previousData) => previousData,
		refetchInterval: 60_000,
	});

	return { data, isLoading, isError, refetch };
}

export function useAcknowledgeIncident() {
	const client = useQueryClient();
	const { t } = useTranslation();

	return useMutation({
		mutationFn: (incidentId: string) => incidentService.acknowledge(incidentId),
		onSuccess: () => toast.success(t('incidents.acknowledge.success')),
		onSettled: () => client.invalidateQueries({ queryKey: ['incidents'] }),
	});
}

export function useUpdateIncidentNote() {
	const client = useQueryClient();
	const { t } = useTranslation();

	return useMutation({
		mutationFn: ({ incidentId, note }: { incidentId: string; note: string }) =>
			incidentService.updateNote(incidentId, note),
		onSuccess: () => toast.success(t('incidents.note.saved')),
		onSettled: () => client.invalidateQueries({ queryKey: ['incidents'] }),
	});
}
