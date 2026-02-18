import { Button } from '@/components/atoms/button.tsx';
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
	DialogTrigger,
} from '@/components/atoms/dialog.tsx';
import { Trash2 } from 'lucide-react';
import { DialogClose } from '@radix-ui/react-dialog';
import { useTranslation } from 'react-i18next';
import useDeleteNotification from '@/features/notifications/hooks/useDeleteNotification.ts';

export default function DeleteNotificationDialogue({ notificationId }: { notificationId: string }) {
	const { t } = useTranslation();
	const { form, onSubmit, openDialogue, setOpenDialogue } = useDeleteNotification(notificationId);

	return (
		<Dialog open={openDialogue} onOpenChange={setOpenDialogue}>
			<DialogTrigger asChild>
				<Button variant="destructive" size="sm" className="cursor-pointer">
					<Trash2 className="h-4 w-4" />
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-md">
				<form onSubmit={form.handleSubmit(onSubmit)} noValidate>
					<DialogHeader className="items-center text-center mb-4 mt-2">
						<div className="flex h-12 w-12 items-center justify-center rounded-full bg-red-100">
							<Trash2 className="h-6 w-6 text-red-600" />
						</div>

						<DialogTitle className="mt-4">{t('notifications.title.remove')} ?</DialogTitle>

						<DialogDescription className="text-sm text-center text-muted-foreground mb-4">
							{t('notifications.description.remove')} ?
						</DialogDescription>
					</DialogHeader>

					<DialogFooter className="grid grid-cols-2 gap-2">
						<DialogClose asChild>
							<Button variant="outline" className="w-full">
								{t('button.cancel')}
							</Button>
						</DialogClose>

						<Button variant="destructive" className="w-full" type="submit">
							{t('button.remove', { entity: t('entity.notification') })}
						</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	);
}
