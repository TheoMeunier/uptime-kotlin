import { useTranslation } from 'react-i18next';
import UpdateNotificationDialogue from '@/features/notifications/components/actions/update-notification-dialogue.tsx';
import useNotificationsSetting from '@/features/notifications/hooks/useNotificationsSetting.ts';
import type { NotificationDetail } from '@/features/notifications/schemas/notifications-reponse.schema.ts';
import DeleteNotificationDialogue from '@/features/notifications/components/actions/delete-notification-dialogue.tsx';

export default function ListingNotification() {
	const { t } = useTranslation();
	const { data, isLoading } = useNotificationsSetting();

	if (isLoading) return <div>Chargement...</div>;

	return (
		<div className="w-full space-y-6">
			<div className="space-y-1">
				<h2 className="text-xl font-semibold">{t('notifications.title.notifications')}</h2>
				<p className="text-sm text-muted-foreground">{t('notifications.description.settings')}</p>
			</div>

			<div>
				{data?.map((notification) => (
					<div key={notification.id} className="flex items-center justify-between border-b p-3">
						<span className="text-sm">{notification.name}</span>

						<ActionNotification notification={notification} />
					</div>
				))}
			</div>
		</div>
	);
}

function ActionNotification({ notification }: { notification: NotificationDetail }) {
	const flattenedData = { ...notification, ...notification?.content };

	return (
		<div className="flex gap-2">
			<UpdateNotificationDialogue notification={flattenedData} />
			<DeleteNotificationDialogue notificationId={notification.id} />
		</div>
	);
}
