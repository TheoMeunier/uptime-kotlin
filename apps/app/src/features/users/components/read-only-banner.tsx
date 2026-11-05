import { Eye } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export default function ReadOnlyBanner() {
	const { t } = useTranslation();

	return (
		<div role="status" className="bg-muted/60 text-muted-foreground flex items-center gap-2 border-b px-4 py-2 text-sm">
			<Eye className="size-4 shrink-0" aria-hidden />
			<p className="min-w-0">
				<span className="text-foreground font-medium">{t('users.read_only.title')}</span>
				<span aria-hidden> · </span>
				{t('users.read_only.description')}
			</p>
		</div>
	);
}
