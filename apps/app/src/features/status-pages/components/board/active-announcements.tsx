import { useTranslation } from 'react-i18next';
import AnnouncementBanner from '@/features/status-pages/components/board/announcement-banner.tsx';
import type { PublicAnnouncement } from '@/features/announcements/schemas/announcement.schema.ts';

export default function ActiveAnnouncements({ announcements }: { announcements: PublicAnnouncement[] }) {
	const { t } = useTranslation();

	if (announcements.length === 0) return null;

	return (
		<section aria-label={t('announcements.public.current')} className="flex flex-col gap-3">
			{announcements.map((announcement) => (
				<AnnouncementBanner key={announcement.id} announcement={announcement} />
			))}
		</section>
	);
}
