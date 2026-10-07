import { useTranslation } from 'react-i18next';
import { Activity } from 'lucide-react';
import { Card, CardContent } from '@/components/atoms/card.tsx';

export default function StatusEmptyState() {
	const { t } = useTranslation();

	return (
		<Card className="border-border bg-card">
			<CardContent className="py-16 text-center">
				<div className="bg-muted mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full p-4">
					<Activity className="text-muted-foreground h-8 w-8" />
				</div>
				<h3 className="text-foreground mb-2 text-lg font-semibold">{t('pages.status_page.empty.title')}</h3>
				<p className="text-muted-foreground">{t('pages.status_page.empty.description')}</p>
			</CardContent>
		</Card>
	);
}
