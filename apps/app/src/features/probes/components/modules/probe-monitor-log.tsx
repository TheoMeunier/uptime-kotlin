import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Card, CardDescription, CardTitle } from '@/components/atoms/card.tsx';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import { Button } from '@/components/atoms/button.tsx';
import { Badge } from '@/components/atoms/badge.tsx';
import PurgeProbeLogsDialogue from '@/features/probes/components/actions/purge-probe-logs-dialogue.tsx';
import ExportProbeLogsButton from '@/features/probes/components/actions/export-probe-logs-button.tsx';
import { getStatusTokens } from '@/lib/status.ts';
import { formatShortDateTime } from '@/lib/datetime.ts';

const filters = [
	{ key: 'all', labelKey: 'monitors.logs.filter_all' },
	{ key: ProbeStatusEnum.SUCCESS, labelKey: 'monitors.logs.filter_success' },
	{ key: ProbeStatusEnum.FAILURE, labelKey: 'monitors.logs.filter_errors' },
] as const;

type FilterKey = 'all' | ProbeStatusEnum;

const SLOW_RESPONSE_MS = 300;

const PAGE_SIZE = 30;

export default function ProbeMonitorLog({ probeId, monitors }: { probeId: string; monitors: Monitor[] }) {
	const { t, i18n } = useTranslation();
	const [activeFilter, setActiveFilter] = useState<FilterKey>('all');
	const [visibleCount, setVisibleCount] = useState(PAGE_SIZE);
	const sentinelRef = useRef<HTMLDivElement>(null);

	const sorted = monitors?.slice().reverse() ?? [];
	const filtered = activeFilter === 'all' ? sorted : sorted.filter((m) => m.status === activeFilter);
	const countByStatus = (status: ProbeStatusEnum) => sorted.filter((m) => m.status === status).length;

	const visible = filtered.slice(0, visibleCount);
	const hasMore = visibleCount < filtered.length;

	useEffect(() => setVisibleCount(PAGE_SIZE), [activeFilter]);

	useEffect(() => {
		const node = sentinelRef.current;
		if (!node || !hasMore) return;

		const observer = new IntersectionObserver(
			(entries) => {
				if (entries[0]?.isIntersecting) {
					setVisibleCount((count) => count + PAGE_SIZE);
				}
			},
			{ rootMargin: '300px' }
		);

		observer.observe(node);
		return () => observer.disconnect();
	}, [hasMore]);

	return (
		<Card className="space-y-0">
			<div className="px-4">
				<div className="flex items-center justify-between gap-3">
					<div>
						<CardTitle className="text-base font-medium">{t('monitors.logs.title')}</CardTitle>
						<CardDescription>{t('monitors.logs.description')}</CardDescription>
					</div>
					<div className="flex items-center gap-3">
						<ExportProbeLogsButton probeId={probeId} />
						<PurgeProbeLogsDialogue probeId={probeId} disabled={sorted.length === 0} />
					</div>
				</div>
			</div>

			<div className="border-border flex items-center gap-2 border-b px-4 pb-2.5">
				{filters.map(({ key, labelKey }) => {
					const count = key === 'all' ? sorted.length : countByStatus(key);
					return (
						<Button
							key={key}
							variant={activeFilter === key ? 'secondary' : 'outline'}
							size="sm"
							onClick={() => setActiveFilter(key)}
							className="h-7 cursor-pointer gap-1.5 rounded-md text-xs"
						>
							{t(labelKey)}
							<Badge variant="secondary" className="tabular h-4 px-1.5 text-[10px]">
								{count}
							</Badge>
						</Button>
					);
				})}
			</div>

			<div className="overflow-x-auto font-mono">
				{visible.map((monitor) => {
					const tokens = getStatusTokens(monitor.status);
					const isSlow = monitor.response_time >= SLOW_RESPONSE_MS;

					return (
						<div
							key={monitor.id}
							className="border-border hover:bg-muted/60 grid min-w-[38rem] grid-cols-[7rem_8rem_5rem_1fr] items-center gap-3 border-b px-4 py-2.5 transition-colors"
						>
							<span
								className={`inline-flex w-fit items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium ${tokens.bg} ${tokens.fg}`}
							>
								<span className={`h-1.5 w-1.5 rounded-full ${tokens.solid}`} />
								{t(tokens.labelKey)}
							</span>

							<span className="text-muted-foreground tabular text-xs">
								{formatShortDateTime(monitor.run_at, i18n.language)}
							</span>

							<span
								className={`tabular text-right text-xs font-medium ${isSlow ? 'text-status-degraded-fg' : 'text-foreground'}`}
							>
								{monitor.response_time} ms
							</span>

							<p className="text-muted-foreground truncate text-xs">
								{monitor.message || t('monitors.logs.no_message')}
							</p>
						</div>
					);
				})}

				{filtered.length === 0 && (
					<div className="text-muted-foreground py-12 text-center text-sm">{t('monitors.logs.empty_filter')}</div>
				)}

				{hasMore && (
					<div ref={sentinelRef} className="text-muted-foreground py-4 text-center text-xs">
						{t('monitors.logs.showing', { count: visible.length, total: filtered.length })}
					</div>
				)}
			</div>
		</Card>
	);
}
