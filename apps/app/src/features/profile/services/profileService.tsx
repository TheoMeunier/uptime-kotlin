import api from '@/api/kyClient.ts';
import type { StoreUpdateProfileSchemaType } from '@/features/profile/hooks/useUpdateProfile.ts';
import type { StoreUpdatePasswordSchemaType } from '@/features/profile/hooks/useUpdatePasswordProfile.ts';

const profileService = {
	async updateProfile(data: StoreUpdateProfileSchemaType) {
		await api
			.post('profile/update', {
				json: data,
			})
			.json();
	},

	async updatePasswordProfile(data: StoreUpdatePasswordSchemaType) {
		await api
			.post('profile/update/password', {
				json: data,
			})
			.json();
	},
};

export default profileService;
