import '@/features/demo/setup.ts';
import { Eye } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export default function DemoBanner() {
	const { t } = useTranslation();

	return (
		<div role="status" className="bg-muted/60 text-muted-foreground flex items-center gap-2 border-b px-4 py-2 text-sm">
			<Eye className="size-4 shrink-0" aria-hidden />
			<p className="min-w-0">
				<span className="text-foreground font-medium">{t('demo.banner_title')}</span>
				<span aria-hidden> · </span>
				{t('demo.banner')}
			</p>
		</div>
	);
}
