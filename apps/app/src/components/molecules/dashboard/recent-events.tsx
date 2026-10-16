import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import { ArrowRight } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import type { DashboardStats } from '@/features/dashboard/schemas/dashboard-stats.schema.ts';
import { getStatusTokens } from '@/lib/status.ts';
import { formatOutageDuration, formatShortDateTime } from '@/lib/datetime.ts';

type RecentEvent = DashboardStats['recent_events'][number];

/** An opening reads as a failure, a recovery as a success, a pause as a pause. */
function toneOf(event: RecentEvent): ProbeStatusEnum {
	if (event.type === 'OPENED') return ProbeStatusEnum.FAILURE;

	return event.closed_reason === 'PROBE_DISABLED' ? ProbeStatusEnum.PAUSE : ProbeStatusEnum.SUCCESS;
}

export default function RecentEvents({ events }: { events: RecentEvent[] }) {
	const { t, i18n } = useTranslation();

	return (
		<Card>
			<CardHeader className="pb-3">
				<CardTitle className="text-sm font-medium">{t('dashboard.events.title')}</CardTitle>
				<CardDescription className="mt-0.5 text-xs">{t('dashboard.events.description')}</CardDescription>
				<CardAction>
					<Button variant="ghost" size="sm" className="text-muted-foreground h-7 px-2 text-xs" asChild>
						<Link to="/incidents">
							{t('dashboard.events.see_all')} <ArrowRight />
						</Link>
					</Button>
				</CardAction>
			</CardHeader>
			<CardContent>
				{events.length === 0 ? (
					<p className="text-muted-foreground py-8 text-center text-sm">{t('dashboard.events.empty')}</p>
				) : (
					<ol className="space-y-0">
						{events.map((event) => {
							const tokens = getStatusTokens(toneOf(event));
							const isOngoing = event.type === 'OPENED' && event.ongoing;
							const label =
								event.type === 'OPENED'
									? t('dashboard.events.opened')
									: t(`incidents.closed_reason.${event.closed_reason ?? 'RECOVERY'}`);
							const detail =
								event.type === 'OPENED'
									? event.message
									: t('dashboard.events.lasted', { duration: formatOutageDuration(event.duration_seconds ?? 0) });

							return (
								<li
									key={`${event.incident_id}-${event.type}`}
									className="border-border flex flex-wrap items-center gap-x-3 gap-y-1 border-b py-2.5 last:border-b-0"
								>
									<span
										className={`h-2 w-2 shrink-0 rounded-full ${tokens.solid} ${isOngoing ? 'animate-pulse' : ''}`}
									/>

									<Link
										to={`/monitors/${event.probe_id}`}
										className="min-w-0 flex-1 truncate text-sm font-medium hover:underline sm:w-40 sm:flex-none sm:shrink-0"
									>
										{event.probe_name}
									</Link>

									<span className={`shrink-0 text-xs font-medium ${tokens.fg}`}>{label}</span>

									<span
										className="text-muted-foreground order-last min-w-0 basis-full truncate text-xs sm:order-none sm:flex-1 sm:basis-0"
										title={detail}
									>
										{detail}
									</span>

									{isOngoing && (
										<span
											className={`${tokens.bg} ${tokens.fg} shrink-0 rounded-full px-2 py-0.5 text-[10px] font-semibold`}
										>
											{t('dashboard.events.ongoing')}
										</span>
									)}

									<time
										className="text-muted-foreground tabular ml-auto shrink-0 text-xs"
										dateTime={event.at.toISOString()}
									>
										{formatShortDateTime(event.at, i18n.language)}
									</time>
								</li>
							);
						})}
					</ol>
				)}
			</CardContent>
		</Card>
	);
}
