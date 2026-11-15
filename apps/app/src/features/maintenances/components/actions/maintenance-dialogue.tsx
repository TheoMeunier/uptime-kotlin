import { useTranslation } from 'react-i18next';
import { Plus } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Dialog, DialogContent, DialogTrigger } from '@/components/atoms/dialog.tsx';
import MaintenanceForm from '@/features/maintenances/components/forms/maintenance-form.tsx';
import useStoreMaintenance from '@/features/maintenances/hooks/useStoreMaintenance.ts';

export default function MaintenanceDialogue() {
	const { t } = useTranslation();
	const { openDialogue, setOpenDialogue, onSubmit, isLoading } = useStoreMaintenance();

	return (
		<Dialog open={openDialogue} onOpenChange={setOpenDialogue}>
			<DialogTrigger asChild>
				<Button>
					<Plus className="size-4" /> {t('maintenances.actions.schedule')}
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-2xl">
				<MaintenanceForm mode="create" onSubmit={onSubmit} isLoading={isLoading} />
			</DialogContent>
		</Dialog>
	);
}
