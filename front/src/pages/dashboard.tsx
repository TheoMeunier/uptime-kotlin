import { useQuery } from '@tanstack/react-query';
import dashboardService from '@/features/dashboard/services/dashboardService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/atoms/table';
import { Skeleton } from '@/components/atoms/skeleton';
import { Button } from '@/components/atoms/button';
import { Activity, CheckCircle2, Timer, XCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import ProbeStatus from '@/features/probes/components/modules/probe-status.tsx';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import StatCard from '@/components/molecules/dashboard/stat-card.tsx';
import StatGraphCard from '@/components/molecules/dashboard/stat-graph-card.tsx';
import IncidentBarCard from '@/components/molecules/dashboard/incident-bar-card.tsx';

export default function Dashboard() {
	const { t } = useTranslation();

	const { data, isLoading } = useQuery({
		queryKey: ['dashboard_stats'],
		queryFn: async () => await dashboardService.getStats(),
	});

	if (isLoading) {
		return (
			<div className="space-y-6">
				<div className="grid gap-3 md:grid-cols-2 lg:grid-cols-4">
					{Array.from({ length: 4 }).map((_, i) => (
						<Skeleton key={i} className="h-28 rounded-xl" />
					))}
				</div>
				<div className="grid gap-3 md:grid-cols-3">
					{Array.from({ length: 3 }).map((_, i) => (
						<Skeleton key={i} className="h-36 rounded-xl" />
					))}
				</div>
				<Skeleton className="h-56 rounded-xl" />
			</div>
		);
	}

	return (
		<div className="space-y-6">
			<div className="flex items-center justify-between">
				<h1 className="text-2xl font-semibold text-foreground">Dashboard</h1>
			</div>

			<section>
				<div className="grid gap-3 md:grid-cols-2 lg:grid-cols-4">
					<StatCard
						title={t('dashboard.title.monitors')}
						value={data?.summary.total_monitors}
						description={t('dashboard.description.monitors')}
						icon={Activity}
						variant="info"
					/>
					<StatCard
						title={t('dashboard.title.monitors_up')}
						value={data?.summary.total_monitors_success}
						description={t('dashboard.description.monitors_up')}
						icon={CheckCircle2}
						variant="success"
					/>
					<StatCard
						title={t('dashboard.title.monitors_down')}
						value={data?.summary.total_monitors_failures}
						description={t('dashboard.description.monitors_down')}
						icon={XCircle}
						variant="danger"
					/>
					<StatCard
						title={t('dashboard.title.uptime')}
						value={`${data?.summary.avg_uptime_percent}%`}
						description={t('dashboard.description.uptime')}
						icon={Timer}
						variant="warning"
					/>
				</div>
			</section>

			{/* Metric cards with sparklines */}
			<section>
				<div className="grid gap-3 md:grid-cols-3">
					<StatGraphCard
						title={t('dashboard.title.response_time_average')}
						value={`${Math.round(data!.metrics_last_days.avg_response_time_ms)} ms`}
						description={t('dashboard.description.latency_average')}
						icon={Timer}
						sparklineColor="#378ADD"
						sparklineType="line"
						sparklineData={data?.latency_spark_line ?? []}
					/>
					<IncidentBarCard data={data?.incident_bar ?? []} description="Sur 24 heures" />
					<StatGraphCard
						title={t('dashboard.title.checks_executed')}
						value={data?.metrics_last_days.count_checks24h}
						description={t('dashboard.description.executing')}
						icon={Activity}
						sparklineColor="#22c55e"
						sparklineType="line"
						sparklineData={data?.check_spark_line ?? []}
					/>
				</div>
			</section>

			{/* Down probes table */}
			<section>
				<Card className="shadow-none">
					<CardHeader className="pb-3">
						<div className="flex items-center justify-between">
							<div>
								<CardTitle className="text-sm font-medium">{t('dashboard.title.monitors_down')}</CardTitle>
								<CardDescription className="text-xs mt-0.5">
									{t('dashboard.description.currently_incidents')}
								</CardDescription>
							</div>
							<Button variant="outline" size="sm" className="text-xs h-7">
								Voir tous les incidents
							</Button>
						</div>
					</CardHeader>
					<CardContent>
						<Table>
							<TableHeader>
								<TableRow>
									<TableHead className="text-xs w-24">{t('dashboard.table.status')}</TableHead>
									<TableHead className="text-xs">{t('dashboard.table.services')}</TableHead>
									<TableHead className="text-xs">{t('dashboard.table.times')}</TableHead>
								</TableRow>
							</TableHeader>
							<TableBody>
								{data?.down_probes.map((probe) => (
									<TableRow key={probe.id}>
										<TableCell className="py-3">
											<ProbeStatus status={ProbeStatusEnum.FAILURE} showLabel={false} />
										</TableCell>
										<TableCell className="py-3">
											<div className="font-medium text-sm">{probe.name}</div>
										</TableCell>
										<TableCell className="py-3">
											<DurationBadge duration={probe.down_duration} />
										</TableCell>
									</TableRow>
								))}
								{data?.down_probes.length === 0 && (
									<TableRow>
										<TableCell colSpan={4} className="text-center text-muted-foreground text-sm py-8">
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

function DurationBadge({ duration }: { duration: string }) {
	const days = parseInt(duration);
	const isCritical = days > 30;
	return (
		<span
			className="inline-flex items-center text-xs font-medium px-2 py-1 rounded-md"
			style={isCritical ? { background: '#FCEBEB', color: '#A32D2D' } : { background: '#FAEEDA', color: '#854F0B' }}
		>
			{duration}
		</span>
	);
}
