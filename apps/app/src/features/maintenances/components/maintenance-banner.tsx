import { useTranslation } from 'react-i18next';
import { CalendarClock, Wrench } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import type { ProbeMaintenance } from '@/features/maintenances/schemas/maintenance.schema.ts';
import { useEndMaintenance } from '@/features/maintenances/hooks/useMaintenanceActions.ts';
import { formatDateTime, formatRelativeDuration } from '@/lib/datetime.ts';

export default function MaintenanceBanner({
	probeId,
	current,
	next,
}: {
	probeId?: string;
	current?: ProbeMaintenance | null;
	next?: ProbeMaintenance | null;
}) {
	const { t, i18n } = useTranslation();
	const { end, isLoading } = useEndMaintenance(probeId);

	if (!current && !next) return null;

	if (!current) {
		return (
			<div className="text-muted-foreground flex items-center gap-2 text-sm">
				<CalendarClock className="size-4 shrink-0" />
				<span>
					{next!.title} — {formatDateTime(next!.starts_at, i18n.language)}
				</span>
			</div>
		);
	}

	return (
		<div className="bg-status-maintenance-bg text-status-maintenance-fg flex flex-col gap-3 rounded-lg px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
			<div className="flex items-center gap-2.5 text-sm">
				<Wrench className="size-4 shrink-0" />
				<div>
					<div className="font-medium">{current.title}</div>
					<div className="text-xs opacity-80">
						{t('maintenances.badge.in_progress', { duration: formatRelativeDuration(current.ends_at) })}
					</div>
				</div>
			</div>

			<Button variant="outline" size="sm" disabled={isLoading} onClick={() => end(current.window_id)}>
				{t('maintenances.actions.end')}
			</Button>
		</div>
	);
}
