import { useTranslation } from 'react-i18next';
import { Table, TableBody, TableHead, TableHeader, TableRow } from '@/components/atoms/table.tsx';
import AnnouncementRow from '@/features/announcements/components/listing/announcement-row.tsx';
import type { Announcement } from '@/features/announcements/schemas/announcement.schema.ts';

export default function ListingAnnouncements({ announcements }: { announcements: Announcement[] }) {
	const { t } = useTranslation();

	return (
		<div className="overflow-x-auto">
			<Table>
				<TableHeader>
					<TableRow>
						<TableHead>{t('announcements.table.title')}</TableHead>
						<TableHead>{t('announcements.table.state')}</TableHead>
						<TableHead>{t('announcements.table.pages')}</TableHead>
						<TableHead className="text-right">{t('announcements.table.actions')}</TableHead>
					</TableRow>
				</TableHeader>

				<TableBody>
					{announcements.map((announcement) => (
						<AnnouncementRow key={announcement.id} announcement={announcement} />
					))}
				</TableBody>
			</Table>
		</div>
	);
}
