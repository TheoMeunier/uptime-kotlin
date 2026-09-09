import { lazy, Suspense } from 'react';
import { useQuery } from '@tanstack/react-query';
import dashboardService from '@/features/dashboard/services/dashboardService';
import { Activity, CheckCircle2, Timer, XCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import Metric from '@/components/molecules/metric.tsx';
import IncidentBarCard from '@/components/molecules/dashboard/incident-bar-card.tsx';
import DashboardSkeleton from '@/components/molecules/dashboard/dashboard-skeleton.tsx';
import RecentEvents from '@/components/molecules/dashboard/recent-events.tsx';
import ErrorState from '@/components/molecules/error-state.tsx';
import { failureCountState, uptimeState } from '@/lib/status.ts';

const Sparkline = lazy(() => import('@/components/molecules/dashboard/sparkline.tsx'));

const SPARKLINE_FALLBACK = <div className="mt-3 w-full" style={{ height: 48 }} />;

export default function Dashboard() {
	const { t } = useTranslation();

	const { data, isLoading, isError, refetch } = useQuery({
		queryKey: ['dashboard_stats'],
		queryFn: async () => await dashboardService.getStats(),
	});

	if (isLoading) return <DashboardSkeleton />;
	if (isError || !data) return <ErrorState onRetry={() => refetch()} />;

	const downCount = data.summary.total_monitors_failures;

	return (
		<div className="space-y-6">
			<section>
				<div className="grid gap-3 md:grid-cols-2 lg:grid-cols-4">
					<Metric
						label={t('dashboard.title.monitors')}
						value={data.summary.total_monitors}
						description={t('dashboard.description.monitors')}
						icon={Activity}
					/>
					<Metric
						label={t('dashboard.title.monitors_up')}
						value={data.summary.total_monitors_success}
						description={t('dashboard.description.monitors_up')}
						icon={CheckCircle2}
					/>
					<Metric
						label={t('dashboard.title.monitors_down')}
						value={downCount}
						description={t('dashboard.description.monitors_down')}
						icon={XCircle}
						state={failureCountState(downCount)}
					/>
					<Metric
						label={t('dashboard.title.uptime')}
						value={`${data.summary.avg_uptime_percent}%`}
						description={t('dashboard.description.uptime')}
						icon={Timer}
						state={uptimeState(data.summary.avg_uptime_percent)}
					/>
				</div>
			</section>

			<section>
				<div className="grid gap-3 md:grid-cols-3">
					<Metric
						label={t('dashboard.title.response_time_average')}
						value={Math.round(data.metrics_last_days.avg_response_time_ms)}
						unit="ms"
						description={t('dashboard.description.latency_average')}
						icon={Timer}
					>
						<Suspense fallback={SPARKLINE_FALLBACK}>
							<Sparkline color="var(--primary)" type="line" data={data.latency_spark_line ?? []} />
						</Suspense>
					</Metric>

					<IncidentBarCard data={data.incident_bar ?? []} description={t('dashboard.description.on_24_hours')} />

					<Metric
						label={t('dashboard.title.checks_executed')}
						value={data.metrics_last_days.count_checks24h}
						description={t('dashboard.description.executing')}
						icon={Activity}
					>
						<Suspense fallback={SPARKLINE_FALLBACK}>
							<Sparkline color="var(--muted-foreground)" type="line" data={data.check_spark_line ?? []} />
						</Suspense>
					</Metric>
				</div>
			</section>

			<section>
				<RecentEvents events={data.recent_events} />
			</section>
		</div>
	);
}
