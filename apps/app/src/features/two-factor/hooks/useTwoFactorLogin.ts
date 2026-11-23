import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { getApiErrorCode } from '@/api/api-error.ts';
import authService from '@/features/auth/services/authServices.ts';
import { SIGN_IN_RESTART_ERRORS } from '@/features/two-factor/lib/two-factor-errors.ts';

export default function useTwoFactorLogin({ onSuccess, onRestart }: { onSuccess: () => void; onRestart: () => void }) {
	const [challengeToken, setChallengeToken] = useState<string | null>(null);

	const mutation = useMutation({
		mutationFn: (code: string) => {
			if (!challengeToken) throw new Error('No sign-in in progress.');
			return authService.loginWithCode(challengeToken, code);
		},
		onSuccess,
		onError: (error) => {
			if (SIGN_IN_RESTART_ERRORS.has(getApiErrorCode(error) ?? '')) cancel();
		},
	});

	function cancel() {
		setChallengeToken(null);
		mutation.reset();
		onRestart();
	}

	return {
		awaitingCode: challengeToken !== null,
		start: setChallengeToken,
		submit: (code: string) => mutation.mutate(code),
		isVerifying: mutation.isPending,
		cancel,
	};
}
