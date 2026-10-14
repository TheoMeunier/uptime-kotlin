import i18n from '@/lang/i18n.ts';
import { registerErrorMessage } from '@/api/api-error.ts';

const translations = {
	en: {
		banner_title: 'Read-only demo',
		banner: 'Explore everything freely: changes are disabled and no alert is sent.',
		read_only: 'This is a read-only demo: changes are disabled.',
		login_title: 'Demo account',
		login_hint: 'The fields are already filled in, just sign in.',
	},
	fr: {
		banner_title: 'Démo en lecture seule',
		banner: 'Explorez librement : les modifications sont désactivées et aucune alerte n’est envoyée.',
		read_only: 'Démo en lecture seule : les modifications sont désactivées.',
		login_title: 'Compte de démonstration',
		login_hint: 'Les champs sont déjà remplis, il suffit de se connecter.',
	},
	de: {
		banner_title: 'Schreibgeschützte Demo',
		banner: 'Alles frei erkunden: Änderungen sind deaktiviert und es werden keine Benachrichtigungen versendet.',
		read_only: 'Dies ist eine schreibgeschützte Demo: Änderungen sind deaktiviert.',
		login_title: 'Demo-Konto',
		login_hint: 'Die Felder sind bereits ausgefüllt, einfach anmelden.',
	},
	es: {
		banner_title: 'Demo de solo lectura',
		banner: 'Explora libremente: los cambios están desactivados y no se envía ninguna alerta.',
		read_only: 'Esta es una demo de solo lectura: los cambios están desactivados.',
		login_title: 'Cuenta de demostración',
		login_hint: 'Los campos ya están rellenados, solo tienes que iniciar sesión.',
	},
};

for (const [language, demo] of Object.entries(translations)) {
	i18n.addResourceBundle(language, 'translation', { demo }, true, false);
}

registerErrorMessage('DEMO_READ_ONLY', () => i18n.t('demo.read_only'));
