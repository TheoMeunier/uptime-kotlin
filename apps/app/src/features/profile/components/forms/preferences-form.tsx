import { useTheme } from 'next-themes';
import { useTranslation } from 'react-i18next';
import { Languages, Monitor, Moon, Sun } from 'lucide-react';
import type { ComponentType } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import { Field, FieldDescription, FieldGroup, FieldLabel, FieldTitle } from '@/components/atoms/field.tsx';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/atoms/select.tsx';
import { ToggleGroup, ToggleGroupItem } from '@/components/atoms/toggle-group.tsx';
import { SUPPORTED_LANGUAGES } from '@/lang/i18n.ts';

const THEMES: { value: string; icon: ComponentType<{ className?: string }> }[] = [
	{ value: 'system', icon: Monitor },
	{ value: 'light', icon: Sun },
	{ value: 'dark', icon: Moon },
];

export default function PreferencesForm() {
	const { t, i18n } = useTranslation();
	const { theme, setTheme } = useTheme();
	const language = i18n.resolvedLanguage ?? 'en';

	return (
		<Card>
			<CardHeader>
				<CardTitle>{t('profile.title.preferences')}</CardTitle>
				<CardDescription>{t('profile.description.preferences')}</CardDescription>
			</CardHeader>

			<CardContent>
				<FieldGroup>
					<Field>
						<FieldLabel htmlFor="language">{t('profile.label.language')}</FieldLabel>

						<Select value={language} onValueChange={(value) => void i18n.changeLanguage(value)}>
							<SelectTrigger id="language" className="w-full">
								<span className="flex min-w-0 items-center gap-2.5">
									<Languages className="text-muted-foreground size-4" />
									<SelectValue>{t(`layout.language.${language}`)}</SelectValue>
								</span>
							</SelectTrigger>

							<SelectContent>
								{SUPPORTED_LANGUAGES.map((code) => (
									<SelectItem key={code} value={code}>
										<span className="text-muted-foreground w-6 shrink-0 font-mono text-[11px] tracking-wider uppercase">
											{code}
										</span>
										{t(`layout.language.${code}`)}
									</SelectItem>
								))}
							</SelectContent>
						</Select>

						<FieldDescription>{t('profile.description.language')}</FieldDescription>
					</Field>

					<Field>
						<FieldTitle id="theme-label">{t('profile.label.theme')}</FieldTitle>

						<ToggleGroup
							type="single"
							spacing={1}
							value={theme ?? 'system'}
							onValueChange={(value) => value && setTheme(value)}
							aria-labelledby="theme-label"
							className="bg-muted/60 w-full rounded-lg p-1"
						>
							{THEMES.map(({ value, icon: Icon }) => (
								<ToggleGroupItem
									key={value}
									value={value}
									className="data-[state=on]:bg-background data-[state=on]:text-foreground data-[state=on]:shadow-sm h-8 min-w-0 flex-1 gap-2 px-2 sm:px-3"
								>
									<Icon className="size-4" />
									<span className="truncate text-xs sm:text-sm">{t(`layout.theme.${value}`)}</span>
								</ToggleGroupItem>
							))}
						</ToggleGroup>

						<FieldDescription>{t('profile.description.theme')}</FieldDescription>
					</Field>
				</FieldGroup>
			</CardContent>
		</Card>
	);
}
