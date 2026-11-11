import authService from '@/features/auth/services/authServices.ts';
import jwtDecode from '@/lib/jwt-decode.ts';
import { registerErrorMessage } from '@/api/api-error.ts';
import i18n from '@/lang/i18n.ts';
import { isRole, roleCan, type Permission, type Role } from '@/features/auth/lib/permissions.ts';

registerErrorMessage('READ_ONLY_ACCOUNT', () => i18n.t('users.errors.read_only'));

function currentRole(): Role | null {
	const token = authService.getAccessToken();
	if (!token) return null;
	const role = jwtDecode.parseJwt(token).role;
	return isRole(role) ? role : null;
}

export function usePermissions() {
	const role = currentRole();

	return {
		role,
		can: (permission: Permission) => roleCan(role, permission),
	};
}
