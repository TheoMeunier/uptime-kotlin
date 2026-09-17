import en from '@/lang/en.ts';
import fr from '@/lang/fr.ts';
import de from '@/lang/de.ts';
import es from '@/lang/es.ts';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import i18n from 'i18next';
import { z } from 'zod';

export const SUPPORTED_LANGUAGES = ['en', 'fr', 'de', 'es'] as const;
export type SupportedLanguage = (typeof SUPPORTED_LANGUAGES)[number];

export const LANGUAGE_STORAGE_KEY = 'uptime-kotlin.language';

const resources = {
	en: { translation: en },
	fr: { translation: fr },
	de: { translation: de },
	es: { translation: es },
};

const ZOD_LOCALES: Record<SupportedLanguage, () => Parameters<typeof z.config>[0]> = {
	en: z.locales.en,
	fr: z.locales.fr,
	de: z.locales.de,
	es: z.locales.es,
};

function normalize(language: string | undefined): SupportedLanguage {
	const base = (language ?? 'en').split('-')[0].toLowerCase();

	return (SUPPORTED_LANGUAGES as readonly string[]).includes(base) ? (base as SupportedLanguage) : 'en';
}

function applyLanguage(language: string) {
	const normalized = normalize(language);

	z.config(ZOD_LOCALES[normalized]());

	if (typeof document !== 'undefined') {
		document.documentElement.lang = normalized;
	}
}

i18n
	.use(LanguageDetector)
	.use(initReactI18next)
	.init({
		resources,
		fallbackLng: 'en',
		supportedLngs: [...SUPPORTED_LANGUAGES],
		load: 'languageOnly',
		nonExplicitSupportedLngs: true,
		detection: {
			order: ['localStorage', 'navigator'],
			lookupLocalStorage: LANGUAGE_STORAGE_KEY,
			caches: ['localStorage'],
		},
		interpolation: { escapeValue: false },
	});

applyLanguage(i18n.language);
i18n.on('languageChanged', applyLanguage);

export default i18n;
