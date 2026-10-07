import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardDescription, CardTitle } from '@/components/atoms/card.tsx';
import ProbeMonitorChartBar from '@/features/probes/components/modules/probe-monitor-chart-bar.tsx';
import ProbeStatus from '@/features/probes/components/modules/probe-status.tsx';
import MaintenanceBadge from '@/features/maintenances/components/maintenance-badge.tsx';
import StatusTimeLegend from '@/features/status-pages/components/board/status-time-legend.tsx';
import { type StatusItem, uptimeTone } from '@/features/status-pages/lib/status-item.ts';

export default function StatusGridCard({ item }: { item: StatusItem }) {
	const { t } = useTranslation();
	const hasFooter = Boolean(item.uptimes || item.down_duration || item.next_maintenance);

	return (
		<Card className="border-border bg-card hover:border-muted-foreground/30 transition-colors">
			<CardContent>
				<div className="flex flex-wrap items-start justify-between gap-2">
					<div className="min-w-0">
						<CardTitle className="text-foreground truncate text-base font-semibold sm:text-lg">
							{item.probe.name}
						</CardTitle>
						<CardDescription className="text-muted-foreground truncate text-xs sm:text-sm">
							{item.probe.url}
						</CardDescription>
					</div>
					{item.maintenance ? (
						<MaintenanceBadge current={item.maintenance} size="sm" />
					) : (
						<ProbeStatus status={item.probe.status} size="sm" />
					)}
				</div>

				<ProbeMonitorChartBar monitors={item.monitors} probeStatus={item.probe.status} barCount={30} />
				<StatusTimeLegend className="mt-1" />

				{hasFooter && (
					<div className="border-border mt-4 flex flex-wrap items-center justify-between gap-x-3 gap-y-1 border-t pt-3 text-xs">
						{item.uptimes ? (
							<span className="text-muted-foreground">
								{t('pages.status_page.uptime_30d')}{' '}
								<span className={`tabular font-semibold ${uptimeTone(item.uptimes.d30)}`}>
									{item.uptimes.d30.toFixed(2)}%
								</span>
							</span>
						) : (
							<span />
						)}

						{item.down_duration && !item.maintenance && (
							<span className="text-status-down-fg tabular font-medium">
								{t('pages.status_page.down_for', { duration: item.down_duration })}
							</span>
						)}

						{item.maintenance_duration && (
							<span className="text-muted-foreground tabular">
								{t('pages.status_page.planned_downtime', { duration: item.maintenance_duration })}
							</span>
						)}

						{!item.maintenance && item.next_maintenance && <MaintenanceBadge next={item.next_maintenance} size="sm" />}
					</div>
				)}
			</CardContent>
		</Card>
	);
}
