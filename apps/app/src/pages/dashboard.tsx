import { lazy, Suspense } from 'react';
import { useQuery } from '@tanstack/react-query';
import dashboardService from '@/features/dashboard/services/dashboardService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/atoms/table';
import { Activity, CheckCircle2, Timer, XCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import ProbeStatus from '@/features/probes/components/modules/probe-status.tsx';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import Metric from '@/components/molecules/metric.tsx';
import IncidentBarCard from '@/components/molecules/dashboard/incident-bar-card.tsx';
import DashboardSkeleton from '@/components/molecules/dashboard/dashboard-skeleton.tsx';
import { failureCountState, uptimeState } from '@/lib/status.ts';

const Sparkline = lazy(() => import('@/components/molecules/dashboard/sparkline.tsx'));

const SPARKLINE_FALLBACK = <div className="mt-3 w-full" style={{ height: 48 }} />;

export default function Dashboard() {
	const { t } = useTranslation();

	const { data, isLoading } = useQuery({
		queryKey: ['dashboard_stats'],
		queryFn: async () => await dashboardService.getStats(),
	});

	if (isLoading || !data) return <DashboardSkeleton />;

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
				<Card className="shadow-none">
					<CardHeader className="pb-3">
						<CardTitle className="text-sm font-medium">{t('dashboard.title.monitors_down')}</CardTitle>
						<CardDescription className="mt-0.5 text-xs">
							{t('dashboard.description.currently_incidents')}
						</CardDescription>
					</CardHeader>
					<CardContent>
						<Table>
							<TableHeader>
								<TableRow>
									<TableHead className="w-24 text-xs">{t('dashboard.table.status')}</TableHead>
									<TableHead className="text-xs">{t('dashboard.table.services')}</TableHead>
									<TableHead className="text-xs">{t('dashboard.table.times')}</TableHead>
								</TableRow>
							</TableHeader>
							<TableBody>
								{data.down_probes.map((probe) => (
									<TableRow key={probe.id}>
										<TableCell className="py-3">
											<ProbeStatus status={ProbeStatusEnum.FAILURE} size="sm" />
										</TableCell>
										<TableCell className="py-3">
											<div className="text-sm font-medium">{probe.name}</div>
										</TableCell>
										<TableCell className="py-3">
											<DurationBadge duration={probe.down_duration} />
										</TableCell>
									</TableRow>
								))}
								{data.down_probes.length === 0 && (
									<TableRow>
										<TableCell colSpan={3} className="text-muted-foreground py-8 text-center text-sm">
											{t('dashboard.table.empty')}
										</TableCell>
									</TableRow>
								)}
							</TableBody>
						</Table>
					</CardContent>
				</Card>
			</section>
		</div>
	);
}

/** Longer outages read as more severe: past 30 days the badge escalates from amber to red. */
function DurationBadge({ duration }: { duration: string }) {
	const days = parseInt(duration, 10);
	const isCritical = Number.isFinite(days) && days > 30;
	const tone = isCritical ? 'bg-status-down-bg text-status-down-fg' : 'bg-status-degraded-bg text-status-degraded-fg';

	return (
		<span className={`tabular inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ${tone}`}>
			{duration}
		</span>
	);
}
