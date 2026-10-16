import { useTranslation } from 'react-i18next';
import { useSearchParams } from 'react-router';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import ErrorState from '@/components/molecules/error-state.tsx';
import OngoingIncidents from '@/features/incidents/components/ongoing-incidents.tsx';
import IncidentSummary from '@/features/incidents/components/incident-summary.tsx';
import IncidentFilters from '@/features/incidents/components/incident-filters.tsx';
import IncidentHistory from '@/features/incidents/components/incident-history.tsx';
import ExportIncidentsButton from '@/features/incidents/components/export-incidents-button.tsx';
import {
	type IncidentFilters as Filters,
	useIncidentHistory,
	useIncidentSummary,
	useOngoingIncidents,
} from '@/features/incidents/hooks/useIncidents.ts';
import { type IncidentPeriod, parseIncidentPeriod } from '@/features/incidents/schemas/incident.schema.ts';

const DEFAULT_PERIOD: IncidentPeriod = '30d';

function useIncidentSearchParams() {
	const [searchParams, setSearchParams] = useSearchParams();

	const filters: Filters = {
		period: parseIncidentPeriod(searchParams.get('period')) ?? DEFAULT_PERIOD,
		probeId: searchParams.get('probe') ?? undefined,
	};
	const page = Math.max(0, Number(searchParams.get('page')) || 0);

	const update = (next: Filters, nextPage = 0) => {
		const params = new URLSearchParams();
		if (next.period !== DEFAULT_PERIOD) params.set('period', next.period);
		if (next.probeId) params.set('probe', next.probeId);
		if (nextPage > 0) params.set('page', String(nextPage));
		setSearchParams(params, { replace: true });
	};

	return { filters, page, update };
}

export default function Incidents() {
	const { t } = useTranslation();
	const { filters, page, update } = useIncidentSearchParams();

	const ongoing = useOngoingIncidents();
	const summary = useIncidentSummary(filters);
	const history = useIncidentHistory(filters, page);

	if (ongoing.isError && summary.isError && history.isError) {
		return (
			<ErrorState
				onRetry={() => {
					ongoing.refetch();
					summary.refetch();
					history.refetch();
				}}
			/>
		);
	}

	return (
		<div className="space-y-4">
			<section className="min-w-0">
				<h1 className="truncate text-2xl font-semibold tracking-tight">{t('incidents.title')}</h1>
				<p className="text-muted-foreground mt-1 text-sm">{t('incidents.description')}</p>
			</section>

			<OngoingIncidents incidents={ongoing.data?.items} isLoading={ongoing.isLoading} />

			<section className="flex flex-col gap-3 pt-2 sm:flex-row sm:items-center sm:justify-between">
				<h2 className="text-lg font-semibold tracking-tight">{t('incidents.history.title')}</h2>
				<div className="flex flex-wrap gap-2">
					<IncidentFilters filters={filters} onChange={(next) => update(next)} />
					<ExportIncidentsButton filters={filters} />
				</div>
			</section>

			<IncidentSummary summary={summary.data} isLoading={summary.isLoading} />

			<Card>
				<CardHeader className="pb-3">
					<CardTitle className="text-sm font-medium">{t('incidents.history.closed')}</CardTitle>
					<CardDescription className="mt-0.5 text-xs">{t('incidents.history.hint')}</CardDescription>
				</CardHeader>
				<CardContent>
					<IncidentHistory
						data={history.data}
						isLoading={history.isLoading}
						isFetching={history.isFetching}
						onPageChange={(next) => update(filters, next)}
					/>
				</CardContent>
			</Card>
		</div>
	);
}
