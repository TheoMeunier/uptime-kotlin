import { useTranslation } from 'react-i18next';
import { Activity } from 'lucide-react';
import { Card, CardContent } from '@/components/atoms/card.tsx';

export default function StatusPagesEmptyState() {
	const { t } = useTranslation();

	return (
		<Card>
			<CardContent className="py-12 text-center">
				<div className="bg-muted mx-auto mb-4 flex size-14 items-center justify-center rounded-full">
					<Activity className="text-muted-foreground size-7" />
				</div>
				<h3 className="text-foreground mb-2 text-lg font-semibold">{t('status_pages.empty.title')}</h3>
				<p className="text-muted-foreground mx-auto max-w-md text-sm">{t('status_pages.empty.description')}</p>
			</CardContent>
		</Card>
	);
}
