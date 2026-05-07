import type { StoreCreateFirstUserSchemaType } from '@/features/setup/hooks/useFirstUserApplicationForm.ts';

const profileService = {
	async getIsFistStartApplication() {
		const response = await fetch('/api/app/status');

		if (!response.ok) {
			throw Error('Failed to fetch application startup status');
		}

		return response.json();
	},

	async createFirstUserApplication(data: StoreCreateFirstUserSchemaType) {
		const response = await fetch('/api/app/setup/first-user', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify(data),
		});

		return response.json();
	},
};

export default profileService;
