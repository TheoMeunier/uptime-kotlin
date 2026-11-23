import { registerErrorMessage } from '@/api/api-error.ts';
import i18n from '@/lang/i18n.ts';

export const TWO_FACTOR_ERRORS = {
	CHALLENGE_INVALID: 'MFA_CHALLENGE_INVALID',
	INVALID_CODE: 'INVALID_TWO_FACTOR_CODE',
	TOO_MANY_ATTEMPTS: 'TOO_MANY_TWO_FACTOR_ATTEMPTS',
	ALREADY_ENABLED: 'TWO_FACTOR_ALREADY_ENABLED',
	SETUP_REQUIRED: 'TWO_FACTOR_SETUP_REQUIRED',
	NOT_ENABLED: 'TWO_FACTOR_NOT_ENABLED',
	OWN: 'OWN_TWO_FACTOR',
	INVALID_CURRENT_PASSWORD: 'INVALID_CURRENT_PASSWORD',
} as const;

export const SIGN_IN_RESTART_ERRORS: ReadonlySet<string> = new Set([
	TWO_FACTOR_ERRORS.CHALLENGE_INVALID,
	TWO_FACTOR_ERRORS.TOO_MANY_ATTEMPTS,
]);

const MESSAGES: Record<(typeof TWO_FACTOR_ERRORS)[keyof typeof TWO_FACTOR_ERRORS], string> = {
	MFA_CHALLENGE_INVALID: 'two_factor.errors.challenge_invalid',
	INVALID_TWO_FACTOR_CODE: 'two_factor.errors.invalid_code',
	TOO_MANY_TWO_FACTOR_ATTEMPTS: 'two_factor.errors.too_many_attempts',
	TWO_FACTOR_ALREADY_ENABLED: 'two_factor.errors.already_enabled',
	TWO_FACTOR_SETUP_REQUIRED: 'two_factor.errors.setup_required',
	TWO_FACTOR_NOT_ENABLED: 'two_factor.errors.not_enabled',
	OWN_TWO_FACTOR: 'two_factor.errors.own',
	INVALID_CURRENT_PASSWORD: 'two_factor.errors.invalid_current_password',
};

Object.entries(MESSAGES).forEach(([code, key]) => registerErrorMessage(code, () => i18n.t(key)));
