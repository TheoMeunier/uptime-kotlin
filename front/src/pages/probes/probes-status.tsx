import { useQuery } from '@tanstack/react-query';
import probeService from '@/features/probes/services/probeService.ts';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import ProbeMonitorChartBar from '@/features/probes/components/modules/probe-monitor-chart-bar.tsx';
import ProbeStatus from '@/features/probes/components/modules/probe-status.tsx';
import { Activity, Clock, Server } from 'lucide-react';
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
				<div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
					{data.map((probe) => (
						<Card key={probe.id} className="border-slate-200 bg-white hover:shadow-md transition-all duration-200">
							<CardHeader>
								<div className="flex justify-between items-start">
									<div className="flex items-center gap-3">
										<div className="p-2 bg-white rounded-md border border-slate-200">
											<Server className="h-5 w-5 text-slate-600" />
										</div>
										<div>
											<CardTitle className="text-lg font-semibold text-slate-900">{probe.probe.name}</CardTitle>
											<p className="text-sm text-slate-500 mt-0.5">
												{probe.monitors.length} monitor
												{probe.monitors.length > 1 ? 's' : ''} actif
												{probe.monitors.length > 1 ? 's' : ''}
											</p>
										</div>
									</div>
									<ProbeStatus status={probe.probe.status} />
								</div>

								<ProbeMonitorChartBar monitors={probe.monitors} probeStatus={probe.status} />
							</CardHeader>
						</Card>
					))}
				</div>

				{data.length === 0 && (
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
	return (
		<div className="min-h-screen bg-slate-50">
			<div className="border-b bg-white">
				<div className="container mx-auto px-6 py-8">
					<Skeleton className="h-8 w-48 mb-2" />
					<Skeleton className="h-4 w-96" />
				</div>
			</div>

			<div className="container mx-auto px-6 py-8">
				<div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
					{[1, 2, 3, 4].map((i) => (
						<Card key={i}>
							<CardContent className="p-6">
								<Skeleton className="h-16 w-full" />
							</CardContent>
						</Card>
					))}
				</div>
			</div>

			<div className="container mx-auto px-6 pb-12">
				<div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
					{[1, 2, 3, 4].map((i) => (
						<Card key={i}>
							<CardHeader>
								<Skeleton className="h-6 w-32" />
							</CardHeader>
							<CardContent>
								<Skeleton className="h-32 w-full" />
							</CardContent>
						</Card>
					))}
				</div>
			</div>
		</div>
	);
}
