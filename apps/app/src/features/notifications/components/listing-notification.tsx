import { useTranslation } from 'react-i18next';
import { BellOff } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import { Badge } from '@/components/atoms/badge.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import UpdateNotificationDialogue from '@/features/notifications/components/actions/update-notification-dialogue.tsx';
import DeleteNotificationDialogue from '@/features/notifications/components/actions/delete-notification-dialogue.tsx';
import CreateNotificationDialogue from '@/features/notifications/components/actions/create-notification-dialogue.tsx';
import useNotificationsSetting from '@/features/notifications/hooks/useNotificationsSetting.ts';
import type { NotificationDetail } from '@/features/notifications/schemas/notifications-reponse.schema.ts';

export default function ListingNotification() {
	const { t } = useTranslation();
	const { data, isLoading } = useNotificationsSetting();

	return (
		<Card>
			<CardHeader className="flex flex-row items-start justify-between gap-4 space-y-0">
				<div>
					<CardTitle>{t('notifications.title.notifications')}</CardTitle>
					<CardDescription className="mt-1.5">{t('notifications.description.settings')}</CardDescription>
				</div>
				<CreateNotificationDialogue />
			</CardHeader>

			<CardContent>
				{isLoading ? (
					<ListingSkeleton />
				) : !data || data.length === 0 ? (
					<EmptyState />
				) : (
					<ul className="divide-border divide-y">
						{data.map((notification) => (
							<li key={notification.id} className="flex items-center justify-between gap-4 py-3">
								<div className="flex min-w-0 items-center gap-2.5">
									<span className="truncate text-sm font-medium">{notification.name}</span>
									<Badge variant="outline" className="shrink-0 font-normal">
										{notification.notification_type}
									</Badge>
									{notification.is_default && (
										<Badge variant="secondary" className="shrink-0 font-normal">
											{t('notifications.label.is_default')}
										</Badge>
									)}
								</div>

								<ActionNotification notification={notification} />
							</li>
						))}
					</ul>
				)}
			</CardContent>
		</Card>
	);
}

function ActionNotification({ notification }: { notification: NotificationDetail }) {
	const flattenedData = { ...notification, ...notification?.content };

	return (
		<div className="flex shrink-0 gap-2">
			<UpdateNotificationDialogue notification={flattenedData} />
			<DeleteNotificationDialogue notificationId={notification.id} />
		</div>
	);
}

function EmptyState() {
	const { t } = useTranslation();

	return (
		<div className="flex flex-col items-center py-10 text-center">
			<span className="bg-muted text-muted-foreground mb-3 flex size-11 items-center justify-center rounded-full">
				<BellOff className="size-5" />
			</span>
			<p className="text-sm font-medium">{t('notifications.empty.title')}</p>
			<p className="text-muted-foreground mt-1 text-sm">{t('notifications.empty.description')}</p>
		</div>
	);
}

function ListingSkeleton() {
	return (
		<ul className="divide-border divide-y">
			{Array.from({ length: 3 }).map((_, i) => (
				<li key={i} className="flex items-center justify-between gap-4 py-3">
					<div className="flex items-center gap-2.5">
						<Skeleton className="h-4 w-32" />
						<Skeleton className="h-5 w-16 rounded-full" />
					</div>
					<div className="flex gap-2">
						<Skeleton className="h-8 w-8 rounded-md" />
						<Skeleton className="h-8 w-8 rounded-md" />
					</div>
				</li>
			))}
		</ul>
	);
}
