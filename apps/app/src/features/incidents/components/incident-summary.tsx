import { useTranslation } from 'react-i18next';
import { Card, CardContent } from '@/components/atoms/card.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import Metric, { MetricRow } from '@/components/molecules/metric.tsx';
import type { IncidentSummary as Summary } from '@/features/incidents/schemas/incident.schema.ts';
import { formatOutageDuration } from '@/lib/datetime.ts';

function longestSeconds(summary: Summary): number | null {
	const longest = summary.longest;
	if (!longest) return null;

	return longest.duration_seconds ?? (Date.now() - longest.started_at.getTime()) / 1000;
}

export default function IncidentSummary({ summary, isLoading }: { summary?: Summary; isLoading: boolean }) {
	const { t } = useTranslation();

	if (isLoading || !summary) {
		return (
			<Card>
				<CardContent>
					<Skeleton className="h-14 w-full" />
				</CardContent>
			</Card>
		);
	}

	const longest = longestSeconds(summary);

	return (
		<Card>
			<CardContent>
				<MetricRow>
					<Metric
						variant="inline"
						label={t('incidents.summary.count')}
						value={summary.count}
						state={summary.ongoing > 0 ? 'down' : 'neutral'}
						description={summary.ongoing > 0 ? t('incidents.summary.ongoing', { count: summary.ongoing }) : undefined}
					/>
					<Metric
						variant="inline"
						label={t('incidents.summary.downtime')}
						value={formatOutageDuration(summary.total_downtime_seconds)}
					/>
					<Metric
						variant="inline"
						label={t('incidents.summary.mttr')}
						value={summary.mttr_seconds != null ? formatOutageDuration(summary.mttr_seconds) : '—'}
						description={summary.mttr_seconds == null ? t('incidents.summary.mttr_empty') : undefined}
					/>
					<Metric
						variant="inline"
						label={t('incidents.summary.longest')}
						value={longest != null ? formatOutageDuration(longest) : '—'}
						description={summary.longest?.probe_name}
					/>
				</MetricRow>
			</CardContent>
		</Card>
	);
}
