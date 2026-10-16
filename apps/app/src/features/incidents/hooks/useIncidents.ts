import { useQuery } from '@tanstack/react-query';
import incidentService from '@/features/incidents/services/incident-service.ts';
import { periodStart, type IncidentPeriod } from '@/features/incidents/schemas/incident.schema.ts';

export const INCIDENTS_PAGE_SIZE = 20;
/** One ongoing incident per probe at most, so this is a ceiling never reached in practice. */
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
