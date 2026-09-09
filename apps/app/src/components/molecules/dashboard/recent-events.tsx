import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import { getStatusTokens } from '@/lib/status.ts';
import type { DashboardStats } from '@/features/dashboard/schemas/dashboard-stats.schema.ts';

type RecentEvent = DashboardStats['recent_events'][number];

export default function RecentEvents({ events }: { events: RecentEvent[] }) {
	const { t, i18n } = useTranslation();

	return (
		<Card>
			<CardHeader className="pb-3">
				<CardTitle className="text-sm font-medium">{t('dashboard.events.title')}</CardTitle>
				<CardDescription className="mt-0.5 text-xs">{t('dashboard.events.description')}</CardDescription>
			</CardHeader>
			<CardContent>
				{events.length === 0 ? (
					<p className="text-muted-foreground py-8 text-center text-sm">{t('dashboard.events.empty')}</p>
				) : (
					<ol className="space-y-0">
						{events.map((event, index) => {
							const tokens = getStatusTokens(event.status);

							return (
								<li
									key={`${event.probe_id}-${event.run_at.getTime()}-${index}`}
									className="border-border flex items-center gap-3 border-b py-2.5 last:border-b-0"
								>
									<span className={`h-2 w-2 shrink-0 rounded-full ${tokens.solid}`} />

									<span className="w-40 shrink-0 truncate text-sm font-medium">{event.probe_name}</span>

									<span className={`shrink-0 text-xs font-medium ${tokens.fg}`}>{t(tokens.labelKey)}</span>

									<span className="text-muted-foreground truncate text-xs">{event.message}</span>

									<time
										className="text-muted-foreground tabular ml-auto shrink-0 text-xs"
										dateTime={event.run_at.toISOString()}
									>
										{event.run_at.toLocaleString(i18n.language, {
											day: '2-digit',
											month: '2-digit',
											hour: '2-digit',
											minute: '2-digit',
										})}
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
