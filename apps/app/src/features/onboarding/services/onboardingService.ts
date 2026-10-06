import { z } from 'zod';
import api from '@/api/kyClient.ts';

const OnboardingStatusSchema = z.object({ completed: z.boolean() });

export type OnboardingStatus = z.infer<typeof OnboardingStatusSchema>;

const onboardingService = {
	async getStatus(): Promise<OnboardingStatus> {
		const response = await api.get('profile/onboarding').json();
		return OnboardingStatusSchema.parse(response);
	},

	async complete(): Promise<OnboardingStatus> {
		const response = await api.post('profile/onboarding/complete').json();
		return OnboardingStatusSchema.parse(response);
	},
};

export default onboardingService;
