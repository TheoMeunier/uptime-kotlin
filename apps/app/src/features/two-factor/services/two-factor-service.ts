import { z } from 'zod';
import api from '@/api/kyClient.ts';
import authService from '@/features/auth/services/authServices.ts';
import '@/features/two-factor/lib/two-factor-errors.ts';

export const TwoFactorStatusSchema = z.object({
	enabled: z.boolean(),
	enabled_at: z.string().nullable().optional(),
	recovery_codes_remaining: z.number(),
});

export type TwoFactorStatus = z.infer<typeof TwoFactorStatusSchema>;

const SetupSchema = z.object({
	secret: z.string().min(1),
	otpauth_uri: z.string().startsWith('otpauth://'),
});

export type TwoFactorSetup = z.infer<typeof SetupSchema>;

const RecoveryCodesSchema = z.object({
	recovery_codes: z.array(z.string()),
});

async function postForRecoveryCodes(path: string, json: object): Promise<string[]> {
	return RecoveryCodesSchema.parse(await api.post(path, { json }).json()).recovery_codes;
}

const twoFactorService = {
	async getStatus(): Promise<TwoFactorStatus> {
		return TwoFactorStatusSchema.parse(await api.get('profile/two-factor').json());
	},

	async setup(): Promise<TwoFactorSetup> {
		return SetupSchema.parse(await api.post('profile/two-factor/setup').json());
	},

	enable(code: string): Promise<string[]> {
		return postForRecoveryCodes('profile/two-factor/enable', { code, current_session_id: authService.getSessionId() });
	},

	async disable(password: string, code: string): Promise<void> {
		await api.post('profile/two-factor/disable', { json: { password, code } });
	},

	regenerateRecoveryCodes(code: string): Promise<string[]> {
		return postForRecoveryCodes('profile/two-factor/recovery-codes', { code });
	},
};

export default twoFactorService;
