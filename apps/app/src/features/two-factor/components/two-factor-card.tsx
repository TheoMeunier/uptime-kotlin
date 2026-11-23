import type { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { Badge } from '@/components/atoms/badge.tsx';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/atoms/card.tsx';

interface TwoFactorCardProps {
	enabled?: boolean;
	children: ReactNode;
	footer?: ReactNode;
}

export default function TwoFactorCard({ enabled, children, footer }: TwoFactorCardProps) {
	const { t } = useTranslation();

	return (
		<Card>
			<CardHeader>
				<CardTitle className="flex items-center gap-2">
					{t('two_factor.title')}
					{enabled !== undefined && (
						<Badge variant={enabled ? 'default' : 'outline'}>
							{t(enabled ? 'two_factor.status.on' : 'two_factor.status.off')}
						</Badge>
					)}
				</CardTitle>
				<CardDescription>{t('two_factor.description')}</CardDescription>
			</CardHeader>
			<CardContent className="flex flex-col gap-3">{children}</CardContent>
			{footer && <CardFooter className="flex flex-wrap gap-2">{footer}</CardFooter>}
		</Card>
	);
}
