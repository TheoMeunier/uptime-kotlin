import { useQuery } from '@tanstack/react-query';
import { useParams } from 'react-router';
import probeService from '@/features/probes/services/probeService.ts';
import { Button } from '@/components/atoms/button.tsx';
import { Pencil } from 'lucide-react';
import { ButtonGroup } from '@/components/atoms/button-group.tsx';
import { Card, CardContent, CardDescription, CardTitle } from '@/components/atoms/card.tsx';
import DeleteProbeDialogue from '@/features/probes/components/actions/delete-probe-dialogue.tsx';
import ProbeMonitorChartBar from '@/features/probes/components/modules/probe-monitor-chart-bar.tsx';
import ProbeChart from '@/features/probes/components/modules/probe-chart.tsx';
import ProbeMonitorLog from '@/features/probes/components/modules/probe-monitor-log.tsx';
import OnOffMonitorProbeDialogue from '@/features/probes/components/actions/on-off-probe-dialogue.tsx';
import ProbeStatus from '@/features/probes/components/modules/probe-status.tsx';
import { useTranslation } from 'react-i18next';
import { Skeleton } from '@/components/atoms/skeleton.tsx';

export function ShowProbe() {
	const { t } = useTranslation();
	const params = useParams();

	const { data, isLoading } = useQuery({
		queryKey: ['probe', params.probeId!],
		queryFn: async () => {
			return probeService.getProbe(params.probeId!);
		},
	});

	if (isLoading) return <ShowProbeSkeleton />;

	return (
		<div className="space-y-8">
			<section className="flex items-center justify-between">
				<div>
					<h1 className="text-3xl font-bold mb-4">{data?.probe.name}</h1>
					<p className="text-gray-600">{data?.probe.url}</p>
				</div>

				<div>
					<ButtonGroup>
						<OnOffMonitorProbeDialogue probeId={data!.probe.id} enabled={data!.probe.enabled} />
						<Button variant="outline">
							<Pencil /> {t('button.actions.edit')}
						</Button>
						<DeleteProbeDialogue probeId={data!.probe.id} />
					</ButtonGroup>
				</div>
			</section>

			<section>
				<Card>
					<CardContent>
						<div className="flex items-center justify-between">
							<div>
								<CardTitle className="text-xl">{t('monitors.title.final_hour')}</CardTitle>
								<CardDescription className="mt-1">
									{t('monitors.description.check_interval', {
										interval: data?.probe.interval,
									})}
								</CardDescription>
							</div>
							<ProbeStatus status={data!.probe.status} />
						</div>
						<ProbeMonitorChartBar monitors={data!.monitors} probeStatus={data!.probe.status} />
						<div className="flex justify-between text-xs text-slate-500 mt-1">
							<span>{t('monitors.description.one_hour_ago')}</span>
							<span>{t('monitors.description.now')}</span>
						</div>
					</CardContent>
				</Card>
			</section>

			<section>
				<ProbeChart monitors={data!.monitors} />
			</section>

			<section>
				<ProbeMonitorLog monitors={data!.monitors} />
			</section>
		</div>
	);
}

function ShowProbeSkeleton() {
	return (
		<div className="space-y-8">
			<section className="flex items-center justify-between">
				<div>
					<Skeleton className="h-9 w-[300px] mb-4" />
					<Skeleton className="h-5 w-[400px]" />
				</div>

				<div className="flex gap-2">
					<Skeleton className="h-10 w-[120px]" />
					<Skeleton className="h-10 w-[100px]" />
					<Skeleton className="h-10 w-[100px]" />
				</div>
			</section>

			<section>
				<Card>
					<CardContent>
						<div className="flex items-center justify-between mb-6">
							<div>
								<Skeleton className="h-6 w-[180px] mb-2" />
								<Skeleton className="h-4 w-[250px]" />
							</div>
							<Skeleton className="h-8 w-[100px] rounded-full" />
						</div>

						<div className="flex gap-1 h-16 items-end">
							{[...Array(60)].map((_, index) => (
								<Skeleton key={index} className="flex-1 rounded-t" style={{ height: `${Math.random() * 60 + 20}%` }} />
							))}
						</div>

						<div className="flex justify-between mt-1">
							<Skeleton className="h-3 w-[100px]" />
							<Skeleton className="h-3 w-[80px]" />
						</div>
					</CardContent>
				</Card>
			</section>

			<section>
				<Card>
					<CardContent>
						<Skeleton className="h-6 w-[200px] mb-4" />
						<Skeleton className="h-[300px] w-full" />
					</CardContent>
				</Card>
			</section>

			<section>
				<Card>
					<CardContent>
						<Skeleton className="h-6 w-[180px] mb-4" />
						<div className="space-y-3">
							{[...Array(8)].map((_, index) => (
								<div key={index} className="flex items-center gap-4 p-3 border rounded-lg">
									<Skeleton className="h-4 w-4 rounded-full" />
									<Skeleton className="h-4 w-[150px]" />
									<Skeleton className="h-4 w-[100px]" />
									<Skeleton className="h-4 flex-1" />
								</div>
							))}
						</div>
					</CardContent>
				</Card>
			</section>
		</div>
	);
}
