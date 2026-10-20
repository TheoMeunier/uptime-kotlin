import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import { Badge } from '@/components/atoms/badge.tsx';
import type { Announcement } from '@/features/announcements/schemas/announcement.schema.ts';

export default function AnnouncementTargets({ announcement }: { announcement: Announcement }) {
	const { t } = useTranslation();
	const { pages, show_on_global } = announcement;

	if (pages.length === 0 && !show_on_global) {
		return <span className="text-status-degraded-fg text-xs">{t('announcements.table.no_target')}</span>;
	}

	return (
		<div className="flex flex-wrap gap-1.5">
			{show_on_global && (
				<Badge variant="outline" asChild>
					<Link to="/status" target="_blank">
						/status
					</Link>
				</Badge>
			)}
			{pages.map((page) => (
				<Badge key={page.id} variant="outline" asChild>
					<Link to={`/status/${page.slug}`} target="_blank" title={page.title}>
						/status/{page.slug}
					</Link>
				</Badge>
			))}
		</div>
	);
}
