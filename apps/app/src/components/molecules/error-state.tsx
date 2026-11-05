import { useTranslation } from 'react-i18next';
import { type LucideIcon, RotateCw, ServerCrash } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Card, CardContent } from '@/components/atoms/card.tsx';

interface ErrorStateProps {
	onRetry?: () => void;
	title?: string;
	description?: string;
	icon?: LucideIcon;
	action?: React.ReactNode;
}

export default function ErrorState({ onRetry, title, description, icon: Icon = ServerCrash, action }: ErrorStateProps) {
	const { t } = useTranslation();

	return (
		<Card>
			<CardContent className="flex flex-col items-center py-12 text-center">
				<span className="bg-status-down-bg text-status-down-fg mb-4 flex size-12 items-center justify-center rounded-full">
					<Icon className="size-6" />
				</span>

				<h2 className="text-base font-semibold">{title ?? t('errors.load.title')}</h2>
				<p className="text-muted-foreground mt-1 max-w-sm text-sm">{description ?? t('errors.load.description')}</p>

				{action && <div className="mt-5">{action}</div>}

				{onRetry && (
					<Button variant="outline" className="mt-5" onClick={onRetry}>
						<RotateCw className="mr-2 size-4" />
						{t('button.retry')}
					</Button>
				)}
			</CardContent>
		</Card>
	);
}
