import { useTranslation } from 'react-i18next';
import { cn } from '@/lib/utils.ts';
import { formatUptime, UPTIME_PERIODS, type UptimePeriod } from '@/features/status-pages/lib/uptime-period.ts';
import { uptimeTone } from '@/features/status-pages/lib/status-item.ts';

interface UptimeLegendProps {
	period: UptimePeriod;
	uptimePercent?: number | null;
	className?: string;
}

export default function UptimeLegend({ period, uptimePercent, className }: UptimeLegendProps) {
	const { t } = useTranslation();
	const { unit, span } = UPTIME_PERIODS[period];
	const showUptime = uptimePercent !== undefined;

	return (
		<div className={cn('text-muted-foreground flex items-center gap-3 text-xs', className)}>
			<span className="shrink-0">{t(`pages.status_page.uptime.ago_${unit}`, { count: span })}</span>
			{showUptime && (
				<>
					<span aria-hidden className="bg-border h-px flex-1" />
					<span className="shrink-0">
						{uptimePercent == null ? (
							t('pages.status_page.uptime.no_data')
						) : (
							<>
								<span className={cn('tabular font-semibold', uptimeTone(uptimePercent))}>
									{formatUptime(uptimePercent)}
								</span>{' '}
								{t('pages.status_page.uptime.label')}
							</>
						)}
					</span>
					<span aria-hidden className="bg-border h-px flex-1" />
				</>
			)}
			{!showUptime && <span className="flex-1" />}
			<span className="shrink-0">
				{t(unit === 'days' && period !== 'D7' ? 'pages.status_page.uptime.today' : 'monitors.description.now')}
			</span>
		</div>
	);
}
