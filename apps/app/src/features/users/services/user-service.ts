import api from '@/api/kyClient.ts';
import { registerErrorMessage } from '@/api/api-error.ts';
import i18n from '@/lang/i18n.ts';
import { UserListSchema, type UserRole } from '@/features/users/schemas/user.schema.ts';
import type { UserFormValues } from '@/features/users/hooks/useUserForm.ts';

registerErrorMessage('LAST_ADMIN', () => i18n.t('users.errors.last_admin'));
registerErrorMessage('OWN_ACCOUNT', () => i18n.t('users.errors.own_account'));
registerErrorMessage('EMAIL_ALREADY_USED', () => i18n.t('users.errors.email_already_used'));

const userService = {
	async getUsers() {
		const response = await api.get('users').json();
		return UserListSchema.parse(response);
	},

	async storeUser(values: UserFormValues) {
		await api.post('users/new', { json: { ...values, name: values.name.trim() } });
	},

	async updateRole(userId: string, role: UserRole) {
		await api.post(`users/${userId}/role`, { json: { role } });
	},

	async removeUser(userId: string) {
		await api.post(`users/${userId}/remove`);
	},
};

export default userService;
