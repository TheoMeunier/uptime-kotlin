import { useTranslation } from 'react-i18next';
import { Trash2 } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import ConfirmDialogue from '@/components/molecules/confirm-dialogue.tsx';
import { useDeleteMaintenance } from '@/features/maintenances/hooks/useMaintenanceActions.ts';

export default function DeleteMaintenanceDialogue({ maintenanceId, title }: { maintenanceId: string; title: string }) {
	const { t } = useTranslation();
	const { remove, isLoading } = useDeleteMaintenance();

	return (
		<ConfirmDialogue
			trigger={
				<Button
					variant="outline"
					size="sm"
					aria-label={t('button.actions.remove')}
					className="text-status-down-fg hover:bg-status-down-bg hover:text-status-down-fg"
				>
					<Trash2 className="size-4" />
				</Button>
			}
			icon={Trash2}
			title={t('maintenances.title.remove')}
			description={t('maintenances.description.remove', { title })}
			confirmLabel={t('button.actions.remove')}
			onConfirm={() => remove(maintenanceId)}
			isPending={isLoading}
		/>
	);
}
