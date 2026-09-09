import { Moon, Sun } from 'lucide-react';
import { useTheme } from 'next-themes';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/atoms/button.tsx';

export default function ThemeToggle() {
	const { resolvedTheme, setTheme } = useTheme();
	const { t } = useTranslation();
	const isDark = resolvedTheme === 'dark';

	return (
		<Button
			variant="ghost"
			size="icon"
			aria-label={t(isDark ? 'layout.theme.switch_to_light' : 'layout.theme.switch_to_dark')}
			onClick={() => setTheme(isDark ? 'light' : 'dark')}
		>
			{isDark ? <Moon className="size-4" /> : <Sun className="size-4" />}
		</Button>
	);
}
