import { useTranslation } from 'react-i18next';
import { cn } from '@/lib/utils.ts';
import { formatDateTime } from '@/lib/datetime.ts';
import { SEVERITY_STYLE } from '@/features/announcements/lib/severity.ts';
import type { PublicAnnouncement } from '@/features/announcements/schemas/announcement.schema.ts';

export default function AnnouncementBanner({ announcement }: { announcement: PublicAnnouncement }) {
	const { t, i18n } = useTranslation();
	const style = SEVERITY_STYLE[announcement.severity];
	const Icon = style.icon;

	return (
		<article className={cn('flex gap-3 rounded-lg border px-4 py-3', style.panel)}>
			<Icon className="mt-0.5 size-5 shrink-0" aria-hidden />

			<div className="min-w-0 flex-1">
				<div className="flex flex-wrap items-baseline gap-x-2 gap-y-0.5">
					<h2 className="font-semibold">{announcement.title}</h2>
					<span className="text-xs font-medium tracking-wide uppercase opacity-80">
						{t(`announcements.severity.${announcement.severity}`)}
					</span>
				</div>

				{announcement.body && (
					<p className="text-foreground/90 mt-1 text-sm break-words whitespace-pre-line">{announcement.body}</p>
				)}

				<p className="mt-2 text-xs opacity-80">
					<span className="tabular">
						{t('announcements.public.posted', { date: formatDateTime(announcement.starts_at, i18n.language) })}
					</span>
					{announcement.ends_at && (
						<span className="tabular">
							{' · '}
							{t('announcements.public.until', { date: formatDateTime(announcement.ends_at, i18n.language) })}
						</span>
					)}
				</p>
			</div>
		</article>
	);
}
