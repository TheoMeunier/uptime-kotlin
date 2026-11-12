import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import profileService from '@/features/profile/services/profileService.tsx';
import { toast } from 'sonner';
import { useTranslation } from 'react-i18next';
import i18n from '@/lang/i18n.ts';
import authService from '@/features/auth/services/authServices.ts';

const storeUpdatePasswordSchema = z
	.object({
		current_password: z.string().min(1),
		password: z.string().min(8),
		password_confirmation: z.string().min(8),
	})
	.refine((data) => data.password === data.password_confirmation, {
		error: () => i18n.t('validation.passwords_mismatch'),
		path: ['password_confirmation'],
	})
	.refine((data) => data.password !== data.current_password, {
		error: () => i18n.t('validation.password_must_differ'),
		path: ['password'],
	});

export type StoreUpdatePasswordSchemaType = z.infer<typeof storeUpdatePasswordSchema>;

export default function useUpdatePasswordProfile() {
	const { t } = useTranslation();

	const form = useForm<StoreUpdatePasswordSchemaType>({
		resolver: zodResolver(storeUpdatePasswordSchema),
	});

	const mutation = useMutation({
		mutationFn: async (data: StoreUpdatePasswordSchemaType) => {
			const email = authService.getUser().email;

			await profileService.updatePasswordProfile(data);
			await authService.login(email, data.password).catch(() => undefined);
		},
		onSuccess: () => {
			form.reset();
			toast.success(t('profile.alerts.update_password'));
		},
	});

	const onSubmit: (data: StoreUpdatePasswordSchemaType) => void = async (data: StoreUpdatePasswordSchemaType) => {
		mutation.mutate(data);
	};

	return {
		form,
		onSubmit,
		isLoading: mutation.isPending,
		errors: form.formState.errors,
	};
}
