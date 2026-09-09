import { useQuery } from '@tanstack/react-query';
import probeService from '@/features/probes/services/probeService.ts';
import { Card, CardContent, CardDescription, CardTitle } from '@/components/atoms/card.tsx';
import ProbeMonitorChartBar from '@/features/probes/components/modules/probe-monitor-chart-bar.tsx';
import ProbeStatus from '@/features/probes/components/modules/probe-status.tsx';
import { Activity, Clock } from 'lucide-react';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import { Badge } from '@/components/atoms/badge.tsx';
import { useTranslation } from 'react-i18next';
import { uptimeState } from '@/lib/status.ts';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';

export default function ProbesStatus() {
	const { t, i18n } = useTranslation();
	const { data, isLoading } = useQuery({
		queryKey: ['probes-status'],
		queryFn: async () => {
			return probeService.getProbesStatus();
		},
		refetchInterval: 120000,
	});

	if (isLoading) return <ProbesStatusSkeleton />;

	const statuses = (data ?? []).map((item) => item.probe.status);
	const total = statuses.length;
	const downCount = statuses.filter((status) => status === ProbeStatusEnum.FAILURE).length;
	const degradedCount = statuses.filter((status) => status === ProbeStatusEnum.WARNING).length;

	const summary =
		downCount > 0
			? {
					dot: 'bg-status-down',
					tone: 'text-status-down-fg',
					label: t('pages.status_page.verdict.down', { count: downCount, total }),
				}
			: degradedCount > 0
				? {
						dot: 'bg-status-degraded',
						tone: 'text-status-degraded-fg',
						label: t('pages.status_page.verdict.degraded', { count: degradedCount, total }),
					}
				: {
						dot: 'bg-status-up',
						tone: 'text-status-up-fg',
						label: t('pages.status_page.verdict.operational', { count: total }),
					};

	return (
		<div className="min-h-screen bg-background">
			<div className="bg-card border-b">
				<div className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
					<div className="flex items-center gap-3 mb-3">
						<div className="p-2 bg-primary/10 rounded-lg shrink-0">
							<Activity className="h-6 w-6 text-primary" />
						</div>
						<div>
							<h1 className="text-xl sm:text-2xl lg:text-3xl font-bold tracking-tight text-foreground">
								{t('pages.status_page.title')}
							</h1>
							<p className="text-sm sm:text-base text-muted-foreground mt-1">{t('pages.status_page.subtitle')}</p>
						</div>
					</div>

					<div className="text-muted-foreground flex flex-wrap items-center gap-x-3 gap-y-1 text-sm">
						<span className="flex items-center gap-1.5 font-medium">
							<span className={`size-2 shrink-0 rounded-full ${summary.dot}`} />
							<span className={summary.tone}>{summary.label}</span>
						</span>

						<span className="flex items-center gap-1.5">
							<Clock className="h-4 w-4 shrink-0" />
							<span className="tabular">
								{t('pages.status_page.description.last_update')}
								{new Date().toLocaleTimeString(i18n.language)}
							</span>
						</span>

						<Badge variant="outline">{t('pages.status_page.description.automatic_refresh')} 2min</Badge>
					</div>
				</div>
			</div>

			<div className="mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 sm:py-12 lg:px-8">
				<div className="grid grid-cols-1 gap-4 sm:gap-6 md:grid-cols-2 xl:grid-cols-3">
					{data?.map((item) => (
						<Card
							key={item.probe.id}
							className="border-border bg-card hover:border-muted-foreground/30 transition-colors"
						>
							<CardContent>
								<div>
									<div className="flex flex-wrap justify-between items-start gap-2">
										<div className="flex items-center min-w-0">
											<div className="min-w-0">
												<CardTitle className="text-base sm:text-lg font-semibold text-foreground truncate">
													{item.probe.name}
												</CardTitle>
												<CardDescription className="text-muted-foreground text-xs sm:text-sm truncate">
													{item.probe.url}
												</CardDescription>
											</div>
										</div>
										<ProbeStatus status={item.probe.status} size="sm" />
									</div>
								</div>

								<ProbeMonitorChartBar monitors={item.monitors} probeStatus={item.probe.status} barCount={30} />

								<div className="text-muted-foreground mt-1 flex justify-between text-xs">
									<span>{t('monitors.description.one_hour_ago')}</span>
									<span>{t('monitors.description.now')}</span>
								</div>

								{/* An outage without a duration reads the same whether it started two minutes
								    or three months ago; the 30-day figure gives the badge its context. */}
								{(item.uptimes || item.down_duration) && (
									<div className="border-border mt-4 flex items-center justify-between border-t pt-3 text-xs">
										{item.uptimes ? (
											<span className="text-muted-foreground">
												{t('pages.status_page.uptime_30d')}{' '}
												<span
													className={`tabular font-semibold ${
														uptimeState(item.uptimes.d30) === 'down'
															? 'text-status-down-fg'
															: uptimeState(item.uptimes.d30) === 'degraded'
																? 'text-status-degraded-fg'
																: 'text-foreground'
													}`}
												>
													{item.uptimes.d30.toFixed(2)}%
												</span>
											</span>
										) : (
											<span />
										)}

										{item.down_duration && (
											<span className="text-status-down-fg tabular font-medium">
												{t('pages.status_page.down_for', { duration: item.down_duration })}
											</span>
										)}
									</div>
								)}
							</CardContent>
						</Card>
					))}
				</div>

				{data?.length === 0 && (
					<Card className="border-border bg-card">
						<CardContent className="text-center py-16">
							<div className="p-4 bg-muted rounded-full w-16 h-16 mx-auto mb-4 flex items-center justify-center">
								<Activity className="h-8 w-8 text-muted-foreground" />
							</div>
							<h3 className="text-foreground mb-2 text-lg font-semibold">{t('pages.status_page.empty.title')}</h3>
							<p className="text-muted-foreground">{t('pages.status_page.empty.description')}</p>
						</CardContent>
					</Card>
				)}
			</div>
		</div>
	);
}

function ProbesStatusSkeleton() {
	/* Mirrors the real card: uniform bars, same count, same footer row, so nothing shifts on load. */
	const BAR_COUNT = 30;
	const CARD_COUNT = 6;

	return (
		<div className="bg-background min-h-screen">
			<div className="bg-card border-b">
				<div className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
					<div className="mb-3 flex items-center gap-3">
						<Skeleton className="h-10 w-10 shrink-0 rounded-lg" />
						<div className="flex flex-col gap-2">
							<Skeleton className="h-7 w-56" />
							<Skeleton className="h-3.5 w-72" />
						</div>
					</div>
					<div className="flex items-center gap-3">
						<div className="flex items-center gap-1.5">
							<Skeleton className="size-2 rounded-full" />
							<Skeleton className="h-3 w-40" />
						</div>
						<div className="flex items-center gap-1.5">
							<Skeleton className="h-4 w-4 rounded" />
							<Skeleton className="h-3 w-36" />
						</div>
						<Skeleton className="h-6 w-28 rounded-full" />
					</div>
				</div>
			</div>

			<div className="mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 sm:py-12 lg:px-8">
				<div className="grid grid-cols-1 gap-4 sm:gap-6 md:grid-cols-2 xl:grid-cols-3">
					{Array.from({ length: CARD_COUNT }).map((_, i) => (
						<Card key={i} className="border-border bg-card">
							<CardContent>
								<div className="flex items-start justify-between gap-2">
									<div className="flex flex-col gap-1.5">
										<Skeleton className="h-4 w-32" />
										<Skeleton className="h-3 w-48" />
									</div>
									<Skeleton className="h-6 w-20 shrink-0 rounded-full" />
								</div>

								<div className="my-3 flex h-8 w-full items-end gap-[3px]">
									{Array.from({ length: BAR_COUNT }).map((_, j) => (
										<Skeleton key={j} className="h-8 flex-1 rounded-[2px]" />
									))}
								</div>

								<div className="mt-1 flex justify-between">
									<Skeleton className="h-2.5 w-16" />
									<Skeleton className="h-2.5 w-10" />
								</div>

								<div className="border-border mt-4 flex items-center justify-between border-t pt-3">
									<Skeleton className="h-3 w-32" />
								</div>
							</CardContent>
						</Card>
					))}
				</div>
			</div>
		</div>
	);
}
