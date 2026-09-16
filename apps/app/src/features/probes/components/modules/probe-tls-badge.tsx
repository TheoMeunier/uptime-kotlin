import { useTranslation } from 'react-i18next';
import { ShieldAlert, ShieldCheck, ShieldX } from 'lucide-react';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/atoms/tooltip.tsx';
import { daysUntil, formatDateTime, formatDayLong } from '@/lib/datetime.ts';

const DEFAULT_WARNING_DAYS = 30;

interface ProbeTlsBadgeProps {
	expiresAt?: string | null;
	checkedAt?: string | null;
	warningDays?: number | null;
}

export default function ProbeTlsBadge({ expiresAt, checkedAt, warningDays }: ProbeTlsBadgeProps) {
	const { t, i18n } = useTranslation();

	if (!expiresAt) return null;

	const days = daysUntil(expiresAt);
	if (days === null) return null;

	const threshold = warningDays && warningDays > 0 ? warningDays : DEFAULT_WARNING_DAYS;
	const { Icon, className, label } =
		days < 0
			? {
					Icon: ShieldX,
					className: 'bg-status-down-bg text-status-down-fg',
					label: t('monitors.tls.expired', { days: Math.abs(days) }),
				}
			: days <= threshold
				? {
						Icon: ShieldAlert,
						className: 'bg-status-degraded-bg text-status-degraded-fg',
						label: days === 0 ? t('monitors.tls.expires_today') : t('monitors.tls.expires_in', { days }),
					}
				: {
						Icon: ShieldCheck,
						className: 'bg-muted text-muted-foreground',
						label: t('monitors.tls.valid_for', { days }),
					};

	return (
		<Tooltip>
			<TooltipTrigger asChild>
				<span className={`inline-flex items-center gap-2 rounded-full px-3 py-1.5 text-sm font-medium ${className}`}>
					<Icon className="h-4 w-4 shrink-0" aria-hidden="true" />
					<span>{label}</span>
				</span>
			</TooltipTrigger>
			<TooltipContent>
				<p>{t('monitors.tls.expires_on', { date: formatDayLong(expiresAt, i18n.language) })}</p>
				{checkedAt && (
					<p className="opacity-75">
						{t('monitors.tls.checked_at', { date: formatDateTime(checkedAt, i18n.language) })}
					</p>
				)}
			</TooltipContent>
		</Tooltip>
	);
}
