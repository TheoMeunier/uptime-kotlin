import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { useLocation, useNavigate, useSearchParams } from 'react-router';
import i18n from '@/lang/i18n.ts';
import authService from '@/features/auth/services/authServices.ts';
import profileService from '@/features/profile/services/profileService.tsx';
import { safeRedirect } from '@/features/auth/lib/redirect.ts';

const changeTemporaryPasswordSchema = z
	.object({
		current_password: z.string().min(1),
		password: z.string().min(8).max(72),
		password_confirmation: z.string().min(8).max(72),
	})
	.refine((data) => data.password === data.password_confirmation, {
		error: () => i18n.t('validation.passwords_mismatch'),
		path: ['password_confirmation'],
	})
	.refine((data) => data.password !== data.current_password, {
		error: () => i18n.t('validation.password_must_differ'),
		path: ['password'],
	});

type ChangeTemporaryPasswordSchema = z.infer<typeof changeTemporaryPasswordSchema>;

function readTemporaryPassword(state: unknown): string {
	if (state && typeof state === 'object' && 'temporaryPassword' in state) {
		const value = (state as { temporaryPassword: unknown }).temporaryPassword;
		if (typeof value === 'string') return value;
	}
	return '';
}

export default function useChangeTemporaryPassword() {
	const navigate = useNavigate();
	const location = useLocation();
	const [searchParams] = useSearchParams();

	const temporaryPassword = readTemporaryPassword(location.state);

	const form = useForm<ChangeTemporaryPasswordSchema>({
		resolver: zodResolver(changeTemporaryPasswordSchema),
		defaultValues: { current_password: temporaryPassword, password: '', password_confirmation: '' },
	});

	const mutation = useMutation({
		mutationFn: async (data: ChangeTemporaryPasswordSchema) => {
			const email = authService.getUser().email;

			await profileService.updatePasswordProfile(data);
			await authService.login(email, data.password);
		},
		onSuccess: () => navigate(safeRedirect(searchParams.get('redirect')), { replace: true }),
	});

	const signOut = async () => {
		await authService.logout();
		navigate('/login', { replace: true });
	};

	return {
		form,
		onSubmit: (data: ChangeTemporaryPasswordSchema) => mutation.mutate(data),
		isLoading: mutation.isPending,
		errors: form.formState.errors,
		askTemporaryPassword: temporaryPassword === '',
		signOut,
	};
}
