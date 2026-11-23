import { useRef } from 'react';
import { useMutation } from '@tanstack/react-query';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate, useSearchParams } from 'react-router';
import { type SubmitHandler, useForm } from 'react-hook-form';
import authService from '@/features/auth/services/authServices.ts';
import { changePasswordPath, safeRedirect } from '@/features/auth/lib/redirect.ts';
import useTwoFactorLogin from '@/features/two-factor/hooks/useTwoFactorLogin.ts';

const loginSchema = z.object({
	email: z.email(),
	password: z.string().min(6),
});

type LoginSchema = z.infer<typeof loginSchema>;

export default function useLoginForm() {
	const navigation = useNavigate();
	const [searchParams] = useSearchParams();
	const form = useForm<LoginSchema>({
		resolver: zodResolver(loginSchema),
	});
	const passwordRef = useRef('');

	const enterApp = () => {
		const target = safeRedirect(searchParams.get('redirect'));

		if (authService.mustChangePassword()) {
			navigation(changePasswordPath(target), { replace: true, state: { temporaryPassword: passwordRef.current } });
			return;
		}

		navigation(target, { replace: true });
	};

	const twoFactor = useTwoFactorLogin({ onSuccess: enterApp, onRestart: () => form.setValue('password', '') });

	const mutation = useMutation({
		mutationFn: async (data: LoginSchema) => {
			passwordRef.current = data.password;
			return authService.login(data.email, data.password);
		},
		onSuccess: (result) => (result.mfaRequired ? twoFactor.start(result.challengeToken) : enterApp()),
	});

	const onsubmit: SubmitHandler<LoginSchema> = async (data: LoginSchema) => {
		mutation.mutate(data);
	};

	return { form, onsubmit, isLoading: mutation.isPending, error: mutation.error, twoFactor };
}
