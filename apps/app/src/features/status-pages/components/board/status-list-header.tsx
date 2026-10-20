import { useTranslation } from 'react-i18next';
import StatusTimeLegend from '@/features/status-pages/components/board/status-time-legend.tsx';
import { LIST_COLUMNS } from '@/features/status-pages/components/board/layout-classes.ts';
import { cn } from '@/lib/utils';
import UptimeLegend from '@/features/status-pages/components/board/uptime-legend.tsx';
import useProbeUptime from '@/features/status-pages/hooks/useProbeUptime.ts';
import { isAggregated } from '@/features/status-pages/lib/uptime-period.ts';

export default function StatusListHeader({ name }: { name?: string | null }) {
	const { t } = useTranslation();
	const { period } = useProbeUptime();

	return (
		<div
			className={cn(
				'text-muted-foreground hidden items-end border-x border-transparent px-5 pb-2 text-xs font-medium',
				LIST_COLUMNS
			)}
		>
			{name ? (
				<h2 className="text-foreground truncate text-lg font-semibold tracking-tight">{name}</h2>
			) : (
				<span>{t('pages.status_page.layout.columns.service')}</span>
			)}
			{isAggregated(period) ? (
				<UptimeLegend period={period} className="font-normal" />
			) : (
				<StatusTimeLegend className="font-normal" />
			)}
		</div>
	);
}
