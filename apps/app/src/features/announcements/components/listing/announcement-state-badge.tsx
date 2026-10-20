import { useTranslation } from 'react-i18next';
import { cn } from '@/lib/utils.ts';
import { formatDateTime } from '@/lib/datetime.ts';
import type { Announcement } from '@/features/announcements/schemas/announcement.schema.ts';

const STATE_STYLE = {
	ACTIVE: 'bg-status-up-bg text-status-up-fg',
	SCHEDULED: 'bg-status-maintenance-bg text-status-maintenance-fg',
	ENDED: 'bg-muted text-muted-foreground',
} as const;

export default function AnnouncementStateBadge({ announcement }: { announcement: Announcement }) {
	const { t, i18n } = useTranslation();
	const { state, starts_at, ends_at } = announcement;

	const detail =
		state === 'SCHEDULED'
			? t('announcements.state_detail.starts', { date: formatDateTime(starts_at, i18n.language) })
			: ends_at
				? t(state === 'ACTIVE' ? 'announcements.state_detail.until' : 'announcements.state_detail.ended', {
						date: formatDateTime(ends_at, i18n.language),
					})
				: t('announcements.state_detail.no_end');

	return (
		<div className="flex flex-col items-start gap-1">
			<span className={cn('rounded-full px-2.5 py-0.5 text-xs font-medium', STATE_STYLE[state])}>
				{t(`announcements.state.${state}`)}
			</span>
			<span className="text-muted-foreground tabular text-xs">{detail}</span>
		</div>
	);
}
