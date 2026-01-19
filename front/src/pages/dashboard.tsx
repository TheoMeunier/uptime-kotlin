import { useQuery } from '@tanstack/react-query';
import dashboardService from '@/features/dashboard/services/dashboardService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/atoms/table';
import { Skeleton } from '@/components/atoms/skeleton';
import { Activity, AlertTriangle, CheckCircle2, Timer, XCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import ProbeStatus from '@/features/probes/components/modules/probe-status.tsx';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';

function StatCard({ title, value, description, icon: Icon, variant = 'neutral' }: any) {
	const variants: Record<string, string> = {
		success: 'text-green-500',
		danger: 'text-red-500',
		warning: 'text-orange-500',
		info: 'text-blue-700',
		neutral: 'text-muted-foreground',
	};

	return (
		<Card className={`border ${variants[variant]} shadow-sm`}>
			<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
				<CardTitle className="text-sm font-medium text-foreground">{title}</CardTitle>
				<Icon className="h-5 w-5" />
			</CardHeader>
			<CardContent>
				<div className="text-2xl font-bold text-foreground">{value}</div>
				{description && <p className="text-xs opacity-80 mt-1">{description}</p>}
			</CardContent>
		</Card>
	);
}

export default function Dashboard() {
	const { t } = useTranslation();

	const { data, isLoading } = useQuery({
		queryKey: ['dashboard_stats'],
		queryFn: async () => await dashboardService.getStats(),
	});

	if (isLoading) {
		return (
			<div className="space-y-6">
				<div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
					{Array.from({ length: 4 }).map((_, i) => (
						<Skeleton key={i} className="h-32 rounded-2xl" />
					))}
				</div>
				<Skeleton className="h-40 rounded-2xl" />
				<Skeleton className="h-64 rounded-2xl" />
			</div>
		);
	}

	return (
		<div className="space-y-10">
			<section className="space-y-4">
				<div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
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

			<section className="space-y-4">
				<h2 className="text-xl font-semibold">{t('dashboard.title.last_24_hours')}</h2>
				<div className="grid gap-4 md:grid-cols-3">
					<StatCard
						title={t('dashboard.title.response_time_average')}
						value={`${Math.round(data!.metrics_last_days.avg_response_time_ms)} ms`}
						description={t('dashboard.description.latency_average')}
						icon={Timer}
					/>
					<StatCard
						title={t('dashboard.title.incidents')}
						value={data?.metrics_last_days.count_incidents24h}
						description={t('dashboard.description.on_24_hours')}
						icon={AlertTriangle}
					/>
					<StatCard
						title={t('dashboard.title.checks_executed')}
						value={data?.metrics_last_days.count_checks24h}
						description={t('dashboard.description.executing')}
						icon={Activity}
					/>
				</div>
			</section>

			<section className="space-y-4">
				<Card>
					<CardHeader>
						<CardTitle>{t('dashboard.title.monitors_down')}</CardTitle>
						<CardDescription>{t('dashboard.description.currently_incidents')}</CardDescription>
					</CardHeader>
					<CardContent>
						<Table>
							<TableHeader>
								<TableRow>
									<TableHead>{t('dashboard.table.status')}</TableHead>
									<TableHead>{t('dashboard.table.services')}</TableHead>
									<TableHead>{t('dashboard.table.times')}</TableHead>
								</TableRow>
							</TableHeader>
							<TableBody>
								{data?.down_probes.map((probe) => (
									<TableRow key={probe.id}>
										<TableCell className="py-2">
											<ProbeStatus status={ProbeStatusEnum.FAILURE} size="size-4" />
										</TableCell>
										<TableCell className="font-medium">{probe.name}</TableCell>
										<TableCell>{probe.down_duration}</TableCell>
									</TableRow>
								))}
								{data?.down_probes.length === 0 && (
									<TableRow>
										<TableCell colSpan={3} className="text-center text-muted-foreground">
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
