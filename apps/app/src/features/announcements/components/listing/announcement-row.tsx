import { TableCell, TableRow } from '@/components/atoms/table.tsx';
import AnnouncementSeverityLabel from '@/features/announcements/components/listing/announcement-severity-label.tsx';
import AnnouncementStateBadge from '@/features/announcements/components/listing/announcement-state-badge.tsx';
import AnnouncementTargets from '@/features/announcements/components/listing/announcement-targets.tsx';
import DeleteAnnouncementDialogue from '@/features/announcements/components/actions/delete-announcement-dialogue.tsx';
import EndAnnouncementButton from '@/features/announcements/components/actions/end-announcement-button.tsx';
import UpdateAnnouncementDialogue from '@/features/announcements/components/actions/update-announcement-dialogue.tsx';
import type { Announcement } from '@/features/announcements/schemas/announcement.schema.ts';

export default function AnnouncementRow({ announcement }: { announcement: Announcement }) {
	return (
		<TableRow>
			<TableCell className="max-w-md">
				<div className="truncate font-medium" title={announcement.title}>
					{announcement.title}
				</div>
				<AnnouncementSeverityLabel severity={announcement.severity} />
			</TableCell>

			<TableCell>
				<AnnouncementStateBadge announcement={announcement} />
			</TableCell>

			<TableCell>
				<AnnouncementTargets announcement={announcement} />
			</TableCell>

			<TableCell>
				<div className="flex justify-end gap-2">
					{announcement.state === 'ACTIVE' && <EndAnnouncementButton announcementId={announcement.id} />}
					<UpdateAnnouncementDialogue announcementId={announcement.id} />
					<DeleteAnnouncementDialogue announcementId={announcement.id} title={announcement.title} />
				</div>
			</TableCell>
		</TableRow>
	);
}
