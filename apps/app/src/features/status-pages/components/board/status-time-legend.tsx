import { useTranslation } from 'react-i18next';
import { cn } from '@/lib/utils';

export default function StatusTimeLegend({ className }: { className?: string }) {
	const { t } = useTranslation();

	return (
		<span className={cn('text-muted-foreground flex justify-between text-xs', className)}>
			<span>{t('monitors.description.one_hour_ago')}</span>
			<span>{t('monitors.description.now')}</span>
		</span>
	);
}
