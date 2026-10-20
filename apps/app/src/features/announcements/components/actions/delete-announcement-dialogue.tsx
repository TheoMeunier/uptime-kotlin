import { useTranslation } from 'react-i18next';
import { Trash2 } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import ConfirmDialogue from '@/components/molecules/confirm-dialogue.tsx';
import { useDeleteAnnouncement } from '@/features/announcements/hooks/useAnnouncementActions.ts';

export default function DeleteAnnouncementDialogue({
	announcementId,
	title,
}: {
	announcementId: string;
	title: string;
}) {
	const { t } = useTranslation();
	const { remove, isLoading } = useDeleteAnnouncement();

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
			title={t('announcements.title.remove')}
			description={t('announcements.description.remove', { title })}
			confirmLabel={t('button.actions.remove')}
			onConfirm={() => remove(announcementId)}
			isPending={isLoading}
		/>
	);
}
