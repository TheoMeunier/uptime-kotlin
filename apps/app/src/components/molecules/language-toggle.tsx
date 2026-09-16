import { Languages } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/atoms/button.tsx';
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuTrigger,
} from '@/components/atoms/dropdown-menu.tsx';
import { SUPPORTED_LANGUAGES } from '@/lang/i18n.ts';

export default function LanguageToggle() {
	const { t, i18n } = useTranslation();
	const current = i18n.resolvedLanguage ?? i18n.language;

	return (
		<DropdownMenu>
			<DropdownMenuTrigger asChild>
				<Button variant="ghost" size="icon" aria-label={t('layout.language.label')}>
					<Languages className="size-4" />
				</Button>
			</DropdownMenuTrigger>

			<DropdownMenuContent align="end" className="min-w-36">
				{SUPPORTED_LANGUAGES.map((language) => (
					<DropdownMenuItem
						key={language}
						onSelect={() => void i18n.changeLanguage(language)}
						className={language === current ? 'font-medium' : undefined}
					>
						<span className="w-6 shrink-0 text-xs uppercase tabular">{language}</span>
						{t(`layout.language.${language}`)}
					</DropdownMenuItem>
				))}
			</DropdownMenuContent>
		</DropdownMenu>
	);
}
