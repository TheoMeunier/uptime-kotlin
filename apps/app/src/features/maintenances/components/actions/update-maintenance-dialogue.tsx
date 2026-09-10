import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { Pencil } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Dialog, DialogContent, DialogTrigger } from '@/components/atoms/dialog.tsx';
import { ScrollArea } from '@/components/atoms/scroll-area.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import MaintenanceForm from '@/features/maintenances/components/forms/maintenance-form.tsx';
import useStoreMaintenance from '@/features/maintenances/hooks/useStoreMaintenance.ts';
import maintenanceService from '@/features/maintenances/services/maintenance-service.ts';
import { toDatetimeLocalInZone } from '@/lib/datetime.ts';

export default function UpdateMaintenanceDialogue({ maintenanceId }: { maintenanceId: string }) {
	const { t } = useTranslation();
	const { openDialogue, setOpenDialogue, onSubmit, isLoading } = useStoreMaintenance(maintenanceId);

	const { data, isLoading: isFetching } = useQuery({
		queryKey: ['maintenance', maintenanceId],
		queryFn: async () => maintenanceService.getMaintenance(maintenanceId),
		enabled: openDialogue,
	});

	return (
		<Dialog open={openDialogue} onOpenChange={setOpenDialogue}>
			<DialogTrigger asChild>
				<Button variant="outline" size="sm" aria-label={t('button.actions.edit')}>
					<Pencil className="size-4" />
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-2xl">
				<ScrollArea className="max-h-[75vh] pr-4">
					{isFetching || !data ? (
						<div className="space-y-4 p-1">
							<Skeleton className="h-6 w-48" />
							<Skeleton className="h-10 w-full" />
							<Skeleton className="h-24 w-full" />
							<Skeleton className="h-10 w-full" />
						</div>
					) : (
						<MaintenanceForm
							mode="update"
							isLoading={isLoading}
							onSubmit={onSubmit}
							defaultValues={{
								title: data.title,
								description: data.description ?? '',
								starts_at: toDatetimeLocalInZone(data.starts_at, data.timezone),
								duration_minutes: Math.round(data.duration_seconds / 60),
								recurrence: data.recurrence,
								recurrence_until: data.recurrence_until
									? toDatetimeLocalInZone(data.recurrence_until, data.timezone)
									: '',
								timezone: data.timezone,
								active: data.active,
								is_public: data.is_public,
								probe_ids: data.probe_ids,
							}}
						/>
					)}
				</ScrollArea>
			</DialogContent>
		</Dialog>
	);
}
