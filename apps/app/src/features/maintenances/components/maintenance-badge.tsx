import { useTranslation } from 'react-i18next';
import { Wrench } from 'lucide-react';
import type { ProbeMaintenance } from '@/features/maintenances/schemas/maintenance.schema.ts';
import { formatRelativeDuration } from '@/lib/datetime.ts';

export default function MaintenanceBadge({
	current,
	next,
	size = 'md',
}: {
	current?: ProbeMaintenance | null;
	next?: ProbeMaintenance | null;
	size?: 'sm' | 'md';
}) {
	const { t } = useTranslation();

	if (!current && !next) return null;

	const padding = size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-1 text-xs';

	if (current) {
		return (
			<span
				className={`bg-status-maintenance-bg text-status-maintenance-fg inline-flex items-center gap-1.5 rounded-full font-medium ${padding}`}
				title={current.title}
			>
				<Wrench className="size-3 shrink-0" />
				{t('maintenances.badge.in_progress', { duration: formatRelativeDuration(current.ends_at) })}
			</span>
		);
	}

	return (
		<span className={`text-muted-foreground inline-flex items-center gap-1.5 ${padding}`} title={next!.title}>
			<Wrench className="size-3 shrink-0" />
			{t('maintenances.badge.scheduled', { duration: formatRelativeDuration(next!.starts_at) })}
		</span>
	);
}
