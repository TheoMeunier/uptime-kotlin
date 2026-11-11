import { Dialog, DialogContent, DialogTrigger } from '@/components/atoms/dialog.tsx';
import { Button } from '@/components/atoms/button.tsx';
import { BellPlus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import useStoreNotification from '@/features/notifications/hooks/useStoreNotification.ts';
import FormNotification from '@/features/notifications/components/forms/form-notification.tsx';
import NotificationTypeEnum from '@/features/notifications/enums/notification-type-enum.ts';

interface CreateNotificationDialogueProps {
	onCreated?: (notificationId: string) => void;
}

export default function CreateNotificationDialogue({ onCreated }: CreateNotificationDialogueProps = {}) {
	const { t } = useTranslation();
	const { openDialogue, setOpenDialogue, onSubmit, isLoading } = useStoreNotification({ onCreated });

	const defaultValues = {
		notification_type: NotificationTypeEnum.DISCORD,
	};

	return (
		<Dialog open={openDialogue} onOpenChange={setOpenDialogue}>
			<DialogTrigger asChild>
				<Button>
					<BellPlus className="mr-2 h-4 w-4" />
					{t('notifications.title.create')}
				</Button>
			</DialogTrigger>

			<DialogContent
				className="flex flex-col overflow-hidden sm:max-w-xl"
				onInteractOutside={(event) => event.preventDefault()}
			>
				<FormNotification mode="create" onSubmit={onSubmit} isLoading={isLoading} defaultValues={defaultValues} />
			</DialogContent>
		</Dialog>
	);
}
