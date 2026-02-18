import { Dialog, DialogContent, DialogTrigger } from '@/components/atoms/dialog.tsx';
import { Button } from '@/components/atoms/button.tsx';
import { SquarePen } from 'lucide-react';
import useUpdateNotification from '@/features/notifications/hooks/useUpdateNotification.ts';
import FormNotification from '@/features/notifications/components/forms/form-notification.tsx';
import type { NotificationDetail } from '@/features/notifications/schemas/notifications-reponse.schema.ts';

export default function UpdateNotificationDialogue({ notification }: { notification: NotificationDetail }) {
	const { openDialogue, setOpenDialogue, onSubmit, isLoading } = useUpdateNotification(notification.id);

	return (
		<Dialog open={openDialogue} onOpenChange={setOpenDialogue}>
			<DialogTrigger asChild>
				<Button variant="outline" size="icon">
					<SquarePen />
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-xl">
				<FormNotification mode="update" onSubmit={onSubmit} isLoading={isLoading} defaultValues={notification} />
			</DialogContent>
		</Dialog>
	);
}
