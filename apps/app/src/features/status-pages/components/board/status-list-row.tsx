import { useTranslation } from 'react-i18next';
import ProbeMonitorChartBar from '@/features/probes/components/modules/probe-monitor-chart-bar.tsx';
import { LIST_COLUMNS } from '@/features/status-pages/components/board/layout-classes.ts';
import { attentionAccent, displayTarget, isDown, type StatusItem } from '@/features/status-pages/lib/status-item.ts';
import { cn } from '@/lib/utils';

const LIST_BAR_COUNT = 60;

/* No badge and no figure: the bars carry the history, the accent flags what needs a look. */
export default function StatusListRow({ item }: { item: StatusItem }) {
	const { t } = useTranslation();
	const accent = attentionAccent(item);
	const showDownFor = Boolean(item.down_duration && !item.maintenance);

	return (
		<li
			className={cn(
				'bg-card text-card-foreground relative flex flex-col gap-3 overflow-hidden rounded-lg border px-4 py-4 transition-colors sm:px-5',
				isDown(item) ? 'border-status-down/40' : 'border-border hover:border-muted-foreground/30',
				LIST_COLUMNS
			)}
		>
			{accent && <span aria-hidden className={`absolute inset-y-0 left-0 w-[3px] ${accent}`} />}

			<div className="min-w-0">
				<p className="text-foreground truncate text-base font-semibold" title={item.probe.name}>
					{item.probe.name}
				</p>
				<p className="text-muted-foreground truncate text-sm" title={item.probe.url}>
					{showDownFor ? (
						<span className="text-status-down-fg tabular font-medium">
							{t('pages.status_page.down_for', { duration: item.down_duration })}
						</span>
					) : (
						displayTarget(item.probe.url)
					)}
				</p>
			</div>

			<ProbeMonitorChartBar
				monitors={item.monitors}
				probeStatus={item.probe.status}
				barCount={LIST_BAR_COUNT}
				compact
			/>
		</li>
	);
}
