import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router';
import probeService from '@/features/probes/services/probeService.ts';
import { Button } from '@/components/atoms/button.tsx';
import { Pencil } from 'lucide-react';
import { ButtonGroup } from '@/components/atoms/button-group.tsx';
import { Card, CardContent, CardDescription, CardTitle } from '@/components/atoms/card.tsx';
import DeleteProbeDialogue from '@/features/probes/components/actions/delete-probe-dialogue.tsx';
import ProbeMonitorChartBar from '@/features/probes/components/modules/probe-monitor-chart-bar.tsx';
import ProbeMonitorLog from '@/features/probes/components/modules/probe-monitor-log.tsx';
import OnOffMonitorProbeDialogue from '@/features/probes/components/actions/on-off-probe-dialogue.tsx';
import ProbeStatus from '@/features/probes/components/modules/probe-status.tsx';
import { useTranslation } from 'react-i18next';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import { useState } from 'react';
import { type ProbeShow, ProbeShowSchema } from '@/features/probes/schemas/probe-response.schema.ts';
import ProbeUptime from '@/features/probes/components/modules/probe-uptime.tsx';
import ProbeResponseTime from '@/features/probes/components/modules/probe-response-time.tsx';

export function ShowProbe() {
	const { t } = useTranslation();
	const params = useParams();
	const [hours, setHours] = useState(1);

	const { data, isLoading, isFetching } = useQuery({
		queryKey: ['probe', params.probeId!, hours],
		queryFn: async () => {
			return probeService.getProbe<ProbeShow>(params.probeId!, hours, ProbeShowSchema);
		},
		placeholderData: (previousData) => previousData,
		refetchInterval: 120000,
	});

	if (isLoading) return <ShowProbeSkeleton />;

	const isRefetching = isFetching && !isLoading;

	return (
		<div className="space-y-4">
			<section className="flex items-center justify-between">
				<div>
					<h1 className="text-3xl font-bold mb-2">{data?.probe.name}</h1>
					<p className="text-gray-500 mb-4">{data?.probe.url}</p>
				</div>

				<div>
					<ButtonGroup>
						<OnOffMonitorProbeDialogue probeId={data!.probe.id} enabled={data!.probe.enabled} />
						<Button variant="outline" asChild>
							<Link to={`/monitors/${data!.probe.id}/edit`}>
								<Pencil /> {t('button.actions.edit')}
							</Link>
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
						<ProbeUptime uptimes={data!.uptimes} />
						<ProbeMonitorChartBar monitors={data!.monitors} probeStatus={data!.probe.status} />
						<div className="flex justify-between text-xs text-slate-500 mt-1">
							<span>{t('monitors.description.one_hour_ago')}</span>
							<span>{t('monitors.description.now')}</span>
						</div>
					</CardContent>
				</Card>
			</section>

			<section>
				{isRefetching ? (
					<Card>
						<CardContent>
							<Skeleton className="h-6 w-[200px] mb-4" />
							<Skeleton className="h-[300px] w-full" />
						</CardContent>
					</Card>
				) : (
					<ProbeResponseTime
						monitors={data!.monitors}
						lastHour={hours}
						setLastHour={setHours}
						monitorStatus={data!.probe.status}
					/>
				)}
			</section>

			<section>
				{isRefetching ? (
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
				) : (
					<ProbeMonitorLog probeId={data!.probe.id} monitors={data!.monitors} />
				)}
			</section>
		</div>
	);
}

function ShowProbeSkeleton() {
	return (
		<div className="space-y-4">
			<section className="flex items-center justify-between">
				<div className="flex flex-col gap-2.5">
					<Skeleton className="h-8 w-64 rounded-lg" />
					<Skeleton className="h-4 w-80" />
				</div>
				<div className="flex">
					<Skeleton className="h-9 w-28 rounded-l-lg rounded-r-none" />
					<Skeleton className="h-9 w-24 rounded-none ml-px" />
					<Skeleton className="h-9 w-24 rounded-r-lg rounded-l-none ml-px" />
				</div>
			</section>

			<section>
				<Card>
					<CardContent>
						<div className="flex items-center justify-between mb-5">
							<div className="flex flex-col gap-2">
								<Skeleton className="h-5 w-44" />
								<Skeleton className="h-3.5 w-60" />
							</div>
							<Skeleton className="h-7 w-24 rounded-full" />
						</div>

						<div className="flex gap-1 mb-3.5">
							<Skeleton className="flex-1 h-7 rounded" />
							<Skeleton className="flex-1 h-7 rounded" />
							<Skeleton className="flex-1 h-7 rounded" />
						</div>

						<div className="flex items-end gap-0.5 h-16">
							{[
								30, 75, 55, 90, 40, 65, 85, 50, 70, 35, 60, 95, 45, 80, 25, 70, 55, 88, 42, 67, 33, 78, 52, 92, 48, 63,
								38, 82, 58, 100,
							].map((h, i) => (
								<Skeleton key={i} className="flex-1 rounded-t-sm rounded-b-none" style={{ height: `${h}%` }} />
							))}
						</div>

						<div className="flex justify-between mt-1.5">
							<Skeleton className="h-2.5 w-20" />
							<Skeleton className="h-2.5 w-14" />
						</div>
					</CardContent>
				</Card>
			</section>

			<section>
				<Card>
					<CardContent>
						<div className="flex items-center justify-between mb-4">
							<Skeleton className="h-5 w-48" />
							<div className="flex">
								<Skeleton className="h-8 w-11 rounded-l-md rounded-r-none" />
								<Skeleton className="h-8 w-11 rounded-none ml-px" />
								<Skeleton className="h-8 w-11 rounded-r-md rounded-l-none ml-px" />
							</div>
						</div>
						<Skeleton className="h-[300px] w-full rounded-lg" />
					</CardContent>
				</Card>
			</section>

			<section>
				<Card>
					<CardContent>
						<Skeleton className="h-5 w-40 mb-4" />
						<div className="space-y-2.5">
							{[130, 110, 145, 120, 100, 135, 115, 125].map((w, i) => (
								<div key={i} className="flex items-center gap-3 p-3 border rounded-lg">
									<Skeleton className="h-4 w-4 rounded-full shrink-0" />
									<Skeleton className={`h-3.5 w-[${w}px]`} />
									<Skeleton className="h-3.5 w-20" />
									<Skeleton className="h-3.5 flex-1" />
								</div>
							))}
						</div>
					</CardContent>
				</Card>
			</section>
		</div>
	);
}
