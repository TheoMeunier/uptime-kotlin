import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Trash2 } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import {
	Dialog,
	DialogClose,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
	DialogTrigger,
} from '@/components/atoms/dialog.tsx';
import { useDeleteMaintenance } from '@/features/maintenances/hooks/useMaintenanceActions.ts';

export default function DeleteMaintenanceDialogue({ maintenanceId, title }: { maintenanceId: string; title: string }) {
	const { t } = useTranslation();
	const [open, setOpen] = useState(false);
	const { remove, isLoading } = useDeleteMaintenance();

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger asChild>
				<Button
					variant="outline"
					size="sm"
					className="text-status-down-fg hover:bg-status-down-bg hover:text-status-down-fg"
				>
					<Trash2 className="size-4" />
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-md">
				<DialogHeader className="items-center text-center">
					<span className="bg-status-down-bg mb-2 flex size-12 items-center justify-center rounded-full">
						<Trash2 className="text-status-down-fg size-6" />
					</span>
					<DialogTitle>{t('maintenances.title.remove')}</DialogTitle>
					<DialogDescription className="text-center">
						{t('maintenances.description.remove', { title })}
					</DialogDescription>
				</DialogHeader>

				<DialogFooter className="mt-4 grid grid-cols-2 gap-2">
					<DialogClose asChild>
						<Button variant="outline" className="w-full" disabled={isLoading}>
							{t('button.cancel')}
						</Button>
					</DialogClose>
					<Button
						className="w-full"
						disabled={isLoading}
						onClick={() => {
							remove(maintenanceId);
							setOpen(false);
						}}
					>
						{t('button.actions.remove')}
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}
