import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import { ArrowRight } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import ErrorState from '@/components/molecules/error-state.tsx';
import OngoingIncidents from '@/features/incidents/components/ongoing-incidents.tsx';
import IncidentSummary from '@/features/incidents/components/incident-summary.tsx';
import IncidentHistory from '@/features/incidents/components/incident-history.tsx';
import {
	type IncidentFilters,
	useIncidentHistory,
	useIncidentSummary,
	useOngoingIncidents,
} from '@/features/incidents/hooks/useIncidents.ts';

export default function ProbeIncidents({ probeId }: { probeId: string }) {
	const { t } = useTranslation();
	const [page, setPage] = useState(0);
	const filters: IncidentFilters = { period: '30d', probeId };

	const ongoing = useOngoingIncidents(probeId);
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

	const ongoingIncidents = ongoing.data?.items ?? [];

	return (
		<div className="space-y-4">
			{ongoingIncidents.length > 0 && (
				<OngoingIncidents incidents={ongoingIncidents} isLoading={false} showProbe={false} />
			)}

			<div className="flex items-center justify-between gap-3">
				<p className="text-muted-foreground text-sm">{t('incidents.filters.periods.30d')}</p>
				<Button variant="ghost" size="sm" className="text-muted-foreground h-7 px-2 text-xs" asChild>
					<Link to={`/incidents?probe=${probeId}&period=365d`}>
						{t('incidents.probe.see_all')} <ArrowRight />
					</Link>
				</Button>
			</div>

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
						onPageChange={setPage}
						showProbe={false}
					/>
				</CardContent>
			</Card>
		</div>
	);
}
