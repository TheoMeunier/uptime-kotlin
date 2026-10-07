import { useTranslation } from 'react-i18next';
import StatusTimeLegend from '@/features/status-pages/components/board/status-time-legend.tsx';
import { LIST_COLUMNS } from '@/features/status-pages/components/board/layout-classes.ts';
import { cn } from '@/lib/utils';

export default function StatusListHeader({ name }: { name?: string | null }) {
	const { t } = useTranslation();

	return (
		<div
			className={cn(
				'text-muted-foreground hidden items-end border-x border-transparent px-5 pb-2 text-xs font-medium',
				LIST_COLUMNS
			)}
		>
			{name ? (
				<h2 className="text-foreground truncate text-lg font-semibold tracking-tight">{name}</h2>
			) : (
				<span>{t('pages.status_page.layout.columns.service')}</span>
			)}
			<StatusTimeLegend className="font-normal" />
		</div>
	);
}
