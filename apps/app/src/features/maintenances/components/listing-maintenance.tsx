import { useTranslation } from 'react-i18next';
import { CalendarClock, Wrench } from 'lucide-react';
import { Badge } from '@/components/atoms/badge.tsx';
import { Button } from '@/components/atoms/button.tsx';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/atoms/table.tsx';
import type { MaintenanceListItem } from '@/features/maintenances/schemas/maintenance.schema.ts';
import DeleteMaintenanceDialogue from '@/features/maintenances/components/actions/delete-maintenance-dialogue.tsx';
import UpdateMaintenanceDialogue from '@/features/maintenances/components/actions/update-maintenance-dialogue.tsx';
import { useEndMaintenance } from '@/features/maintenances/hooks/useMaintenanceActions.ts';
import { formatDateTime, formatDuration, formatRelativeDuration } from '@/lib/datetime.ts';

export default function ListingMaintenance({ maintenances }: { maintenances: MaintenanceListItem[] }) {
	const { t, i18n } = useTranslation();
	const { end, isLoading } = useEndMaintenance();

	return (
		<div className="overflow-x-auto">
			<Table>
				<TableHeader>
					<TableRow>
						<TableHead>{t('maintenances.table.title')}</TableHead>
						<TableHead>{t('maintenances.table.state')}</TableHead>
						<TableHead>{t('maintenances.table.recurrence')}</TableHead>
						<TableHead>{t('maintenances.table.duration')}</TableHead>
						<TableHead>{t('maintenances.table.monitors')}</TableHead>
						<TableHead className="text-right">{t('maintenances.table.actions')}</TableHead>
					</TableRow>
				</TableHeader>

				<TableBody>
					{maintenances.map((maintenance) => (
						<TableRow key={maintenance.id}>
							<TableCell>
								<div className="font-medium">{maintenance.title}</div>
								<div className="text-muted-foreground text-xs">{maintenance.timezone}</div>
							</TableCell>

							<TableCell>
								{maintenance.current_occurrence ? (
									<span className="bg-status-maintenance-bg text-status-maintenance-fg inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium">
										<Wrench className="size-3 shrink-0" />
										{t('maintenances.badge.in_progress', {
											duration: formatRelativeDuration(maintenance.current_occurrence.ends_at),
										})}
									</span>
								) : maintenance.next_occurrence ? (
									<span className="text-muted-foreground inline-flex items-center gap-1.5 text-xs">
										<CalendarClock className="size-3.5 shrink-0" />
										{formatDateTime(maintenance.next_occurrence.starts_at, i18n.language, maintenance.timezone)}
									</span>
								) : (
									<span className="text-muted-foreground text-xs">{t('maintenances.table.no_occurrence')}</span>
								)}
							</TableCell>

							<TableCell>
								<Badge variant="outline">{t(`maintenances.recurrence.${maintenance.recurrence}`)}</Badge>
								{!maintenance.active && (
									<Badge variant="outline" className="ml-2">
										{t('maintenances.table.inactive')}
									</Badge>
								)}
							</TableCell>

							<TableCell className="tabular">{formatDuration(maintenance.duration_seconds)}</TableCell>

							<TableCell className="tabular">{maintenance.probe_count}</TableCell>

							<TableCell>
								<div className="flex justify-end gap-2">
									{maintenance.current_occurrence && (
										<Button variant="outline" size="sm" disabled={isLoading} onClick={() => end(maintenance.id)}>
											{t('maintenances.actions.end')}
										</Button>
									)}
									<UpdateMaintenanceDialogue maintenanceId={maintenance.id} />
									<DeleteMaintenanceDialogue maintenanceId={maintenance.id} title={maintenance.title} />
								</div>
							</TableCell>
						</TableRow>
					))}
				</TableBody>
			</Table>
		</div>
	);
}
