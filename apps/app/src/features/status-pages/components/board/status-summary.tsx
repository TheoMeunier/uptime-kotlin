import { useTranslation } from 'react-i18next';
import { Clock } from 'lucide-react';
import { Badge } from '@/components/atoms/badge.tsx';
import { formatTime } from '@/lib/datetime.ts';
import { type StatusItem, summarize } from '@/features/status-pages/lib/status-item.ts';

const VERDICT_STYLE = {
	down: { dot: 'bg-status-down', tone: 'text-status-down-fg' },
	degraded: { dot: 'bg-status-degraded', tone: 'text-status-degraded-fg' },
	operational: { dot: 'bg-status-up', tone: 'text-status-up-fg' },
} as const;

export default function StatusSummary({ items }: { items: StatusItem[] }) {
	const { t, i18n } = useTranslation();
	const { verdict, maintenanceCount } = summarize(items);
	const style = VERDICT_STYLE[verdict.kind];

	return (
		<div className="text-muted-foreground flex flex-wrap items-center gap-x-3 gap-y-1 text-sm">
			<span className="flex items-center gap-1.5 font-medium">
				<span className={`size-2 shrink-0 rounded-full ${style.dot}`} />
				<span className={style.tone}>
					{t(`pages.status_page.verdict.${verdict.kind}`, { count: verdict.count, total: verdict.total })}
				</span>
			</span>

			{maintenanceCount > 0 && (
				<span className="text-status-maintenance-fg flex items-center gap-1.5">
					<span className="bg-status-maintenance size-2 shrink-0 rounded-full" />
					{t('pages.status_page.verdict.maintenance', { count: maintenanceCount })}
				</span>
			)}

			<span className="flex items-center gap-1.5">
				<Clock className="h-4 w-4 shrink-0" />
				<span className="tabular">
					{t('pages.status_page.description.last_update')}
					{formatTime(new Date(), i18n.language, { withSeconds: true })}
				</span>
			</span>

			<Badge variant="outline">{t('pages.status_page.description.automatic_refresh')} 2min</Badge>
		</div>
	);
}
