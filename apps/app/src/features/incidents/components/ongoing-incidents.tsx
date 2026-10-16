import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import { CircleCheck } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import type { Incident } from '@/features/incidents/schemas/incident.schema.ts';
import { formatDateTime, formatOutageDuration } from '@/lib/datetime.ts';

function useNow(active: boolean): number {
	const [now, setNow] = useState(() => Date.now());

	useEffect(() => {
		if (!active) return;
		const id = setInterval(() => setNow(Date.now()), 1_000);
		return () => clearInterval(id);
	}, [active]);

	return now;
}

export default function OngoingIncidents({
	incidents,
	isLoading,
	showProbe = true,
}: {
	incidents?: Incident[];
	isLoading: boolean;
	showProbe?: boolean;
}) {
	const { t, i18n } = useTranslation();
	const now = useNow((incidents?.length ?? 0) > 0);

	return (
		<Card>
			<CardHeader className="pb-3">
				<CardTitle className="text-sm font-medium">{t('incidents.ongoing.title')}</CardTitle>
				<CardDescription className="mt-0.5 text-xs">{t('incidents.ongoing.description')}</CardDescription>
			</CardHeader>
			<CardContent>
				{isLoading ? (
					<Skeleton className="h-10 w-full" />
				) : !incidents || incidents.length === 0 ? (
					<p className="text-muted-foreground flex items-center gap-2 py-2 text-sm">
						<CircleCheck className="text-status-up-fg size-4 shrink-0" />
						{t('incidents.ongoing.empty')}
					</p>
				) : (
					<ul>
						{incidents.map((incident) => (
							<li
								key={incident.id}
								className="border-border flex flex-wrap items-center gap-x-3 gap-y-1 border-b py-2.5 last:border-b-0"
							>
								<span className="bg-status-down h-2 w-2 shrink-0 animate-pulse rounded-full" />

								{showProbe && (
									<Link
										to={`/monitors/${incident.probe_id}?tab=incidents`}
										className="w-44 shrink-0 truncate text-sm font-medium hover:underline"
									>
										{incident.probe_name}
									</Link>
								)}

								<span
									className="text-muted-foreground order-last min-w-0 basis-full truncate text-xs sm:order-none sm:flex-1 sm:basis-0"
									title={incident.opening_message}
								>
									{incident.opening_message}
								</span>

								<span className="text-muted-foreground shrink-0 text-xs">
									{t('incidents.ongoing.since', { time: formatDateTime(incident.started_at, i18n.language) })}
								</span>

								<span className="bg-status-down-bg text-status-down-fg tabular shrink-0 rounded-full px-2.5 py-0.5 text-xs font-semibold">
									{formatOutageDuration((now - incident.started_at.getTime()) / 1000)}
								</span>
							</li>
						))}
					</ul>
				)}
			</CardContent>
		</Card>
	);
}
