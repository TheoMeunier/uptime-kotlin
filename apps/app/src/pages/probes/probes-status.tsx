import { useQuery } from '@tanstack/react-query';
import probeService from '@/features/probes/services/probeService.ts';
import { Card, CardContent, CardDescription, CardTitle } from '@/components/atoms/card.tsx';
import ProbeMonitorChartBar from '@/features/probes/components/modules/probe-monitor-chart-bar.tsx';
import ProbeStatus from '@/features/probes/components/modules/probe-status.tsx';
import { Activity, Clock } from 'lucide-react';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import { Badge } from '@/components/atoms/badge.tsx';
import { useTranslation } from 'react-i18next';

export default function ProbesStatus() {
	const { t } = useTranslation();
	const { data, isLoading } = useQuery({
		queryKey: ['probes-status'],
		queryFn: async () => {
			return probeService.getProbesStatus();
		},
		refetchInterval: 120000,
	});

	if (isLoading) return <ProbesStatusSkeleton />;

	return (
		<div className="min-h-screen bg-slate-50">
			<div className="border-b bg-white shadow-sm">
				<div className="container mx-auto py-8">
					<div className="flex items-center gap-3 mb-3">
						<div className="p-2 bg-primary/10 rounded-lg">
							<Activity className="h-6 w-6 text-primary" />
						</div>
						<div>
							<h1 className="text-3xl font-bold tracking-tight text-slate-900">{t('pages.status_page.title')}</h1>
							<p className="text-slate-600 mt-1">{t('pages.status_page.subtitle')}</p>
						</div>
					</div>

					<div className="flex items-center gap-2 text-sm text-slate-500">
						<Clock className="h-4 w-4" />
						<span>
							{t('pages.status_page.description.last_update')}
							{new Date().toLocaleTimeString('fr-FR')}
						</span>
						<Badge variant="outline" className="ml-2">
							{t('pages.status_page.description.automatic_refresh')} 2min
						</Badge>
					</div>
				</div>
			</div>

			<div className="w-[98rem] mx-auto py-12">
				<div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
					{data?.map((item) => (
						<Card key={item.probe.id} className="border-slate-200 bg-white hover:shadow-md transition-all duration-200">
							<CardContent>
								<div>
									<div className="flex justify-between items-start">
										<div className="flex items-center">
											<div>
												<CardTitle className="text-lg font-semibold text-slate-900">{item.probe.name}</CardTitle>
												<CardDescription className="text-gray-500 text-sm">{item.probe.url}</CardDescription>
											</div>
										</div>
										<ProbeStatus status={item.probe.status} size="sm" />
									</div>
								</div>

								<ProbeMonitorChartBar monitors={item.monitors} probeStatus={item.probe.status} />

								<div className="flex justify-between text-xs text-slate-500 mt-1">
									<span>{t('monitors.description.one_hour_ago')}</span>
									<span>{t('monitors.description.now')}</span>
								</div>
							</CardContent>
						</Card>
					))}
				</div>

				{data?.length === 0 && (
					<Card className="border-slate-200 bg-white">
						<CardContent className="text-center py-16">
							<div className="p-4 bg-slate-100 rounded-full w-16 h-16 mx-auto mb-4 flex items-center justify-center">
								<Activity className="h-8 w-8 text-slate-400" />
							</div>
							<h3 className="text-lg font-semibold text-slate-900 mb-2">Aucune sonde disponible</h3>
							<p className="text-slate-600">Commencez par ajouter des sondes pour surveiller votre infrastructure</p>
						</CardContent>
					</Card>
				)}
			</div>
		</div>
	);
}

function ProbesStatusSkeleton() {
	const BAR_HEIGHTS = [
		[40, 75, 55, 90, 35, 65, 85, 50, 70, 45, 60, 95, 30, 80, 58, 100],
		[60, 30, 85, 45, 70, 55, 100, 40, 78, 52, 35, 90, 65, 48, 82, 25],
		[50, 88, 42, 67, 33, 78, 95, 58, 72, 38, 62, 46, 85, 28, 75, 55],
		[70, 45, 90, 30, 65, 80, 50, 38, 95, 60, 75, 42, 55, 100, 33, 68],
		[35, 82, 58, 72, 44, 96, 28, 63, 87, 51, 40, 76, 32, 92, 67, 48],
		[80, 55, 38, 92, 48, 63, 28, 75, 100, 42, 70, 35, 88, 57, 44, 78],
	];

	return (
		<div className="min-h-screen bg-slate-50">
			<div className="border-b bg-white shadow-sm">
				<div className="container mx-auto py-8">
					<div className="flex items-center gap-3 mb-3">
						<Skeleton className="h-10 w-10 rounded-lg shrink-0" />
						<div className="flex flex-col gap-2">
							<Skeleton className="h-7 w-56" />
							<Skeleton className="h-3.5 w-72" />
						</div>
					</div>
					<div className="flex items-center gap-2">
						<Skeleton className="h-4 w-4 rounded" />
						<Skeleton className="h-3 w-44" />
						<Skeleton className="h-6 w-28 rounded-full ml-2" />
					</div>
				</div>
			</div>

			<div className="w-[98rem] mx-auto py-12">
				<div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
					{BAR_HEIGHTS.map((heights, i) => (
						<Card key={i} className="border-slate-200 bg-white">
							<CardContent>
								<div className="flex justify-between items-start mb-3.5">
									<div className="flex flex-col gap-1.5">
										<Skeleton className="h-4 w-32" />
										<Skeleton className="h-3 w-48" />
									</div>
									<Skeleton className="h-6 w-16 rounded-full" />
								</div>

								<div className="flex items-end gap-0.5 h-12">
									{heights.map((h, j) => (
										<Skeleton key={j} className="flex-1 rounded-t-sm rounded-b-none" style={{ height: `${h}%` }} />
									))}
								</div>

								<div className="flex justify-between mt-1.5">
									<Skeleton className="h-2.5 w-16" />
									<Skeleton className="h-2.5 w-12" />
								</div>
							</CardContent>
						</Card>
					))}
				</div>
			</div>
		</div>
	);
}
