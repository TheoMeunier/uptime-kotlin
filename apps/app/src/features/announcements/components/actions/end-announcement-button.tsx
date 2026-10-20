import { useTranslation } from 'react-i18next';
import { Button } from '@/components/atoms/button.tsx';
import { useEndAnnouncement } from '@/features/announcements/hooks/useAnnouncementActions.ts';

export default function EndAnnouncementButton({ announcementId }: { announcementId: string }) {
	const { t } = useTranslation();
	const { end, isLoading } = useEndAnnouncement();

	return (
		<Button variant="outline" size="sm" disabled={isLoading} onClick={() => end(announcementId)}>
			{t('announcements.actions.end')}
		</Button>
	);
}
