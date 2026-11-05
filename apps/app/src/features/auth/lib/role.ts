import authService from '@/features/auth/services/authServices.ts';
import jwtDecode from '@/lib/jwt-decode.ts';
import { registerErrorMessage } from '@/api/api-error.ts';
import i18n from '@/lang/i18n.ts';

registerErrorMessage('READ_ONLY_ACCOUNT', () => i18n.t('users.errors.read_only'));

export function useRole() {
	const token = authService.getAccessToken();
	const isAdmin = token ? jwtDecode.parseJwt(token).role !== 'viewer' : false;

	return { isAdmin, isViewer: !isAdmin };
}
