import { Link } from 'react-router';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/atoms/button.tsx';
import ErrorPage from '@/components/molecules/error-page.tsx';

export default function NotFoundPage() {
	const { t } = useTranslation();

	return (
		<ErrorPage
			code="404"
			title={t('errors.not_found.title')}
			description={t('errors.not_found.description')}
			actions={
				<>
					<Button asChild>
						<Link to="/dashboard">{t('errors.actions.dashboard')}</Link>
					</Button>
					<Button variant="outline" asChild>
						<Link to="/status">{t('errors.actions.status')}</Link>
					</Button>
				</>
			}
		/>
	);
}
