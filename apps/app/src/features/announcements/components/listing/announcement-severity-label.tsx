import { useTranslation } from 'react-i18next';
import { cn } from '@/lib/utils.ts';
import { SEVERITY_STYLE } from '@/features/announcements/lib/severity.ts';
import type { AnnouncementSeverity } from '@/features/announcements/schemas/announcement.schema.ts';

export default function AnnouncementSeverityLabel({ severity }: { severity: AnnouncementSeverity }) {
	const { t } = useTranslation();
	const style = SEVERITY_STYLE[severity];
	const Icon = style.icon;

	return (
		<span className={cn('inline-flex items-center gap-1.5 text-xs font-medium', style.tone)}>
			<Icon className="size-3.5 shrink-0" aria-hidden />
			{t(`announcements.severity.${severity}`)}
		</span>
	);
}
