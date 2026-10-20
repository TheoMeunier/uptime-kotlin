import { useTranslation } from 'react-i18next';
import { Megaphone } from 'lucide-react';

export default function AnnouncementsEmptyState() {
	const { t } = useTranslation();

	return (
		<div className="py-16 text-center">
			<div className="bg-muted mx-auto mb-4 flex size-16 items-center justify-center rounded-full">
				<Megaphone className="text-muted-foreground size-8" />
			</div>
			<h3 className="text-foreground mb-2 text-lg font-semibold">{t('announcements.empty.title')}</h3>
			<p className="text-muted-foreground mx-auto max-w-md">{t('announcements.empty.description')}</p>
		</div>
	);
}
