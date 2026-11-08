import { useTranslation } from 'react-i18next';
import { WEBSITE_URL } from '@/lib/links.ts';

export default function StatusBoardFooter() {
	const { t } = useTranslation();

	return (
		<footer className="text-muted-foreground/70 mt-auto pt-4 pb-6 text-center text-xs">
			{t('pages.status_page.powered_by')}{' '}
			<a
				href={WEBSITE_URL}
				target="_blank"
				rel="noopener noreferrer"
				className="hover:text-foreground focus-visible:ring-ring rounded-sm font-medium transition-colors outline-none focus-visible:ring-2"
			>
				Uptime Kotlin
			</a>
		</footer>
	);
}
