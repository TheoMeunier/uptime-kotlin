import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import twoFactorService from '@/features/two-factor/services/two-factor-service.ts';

const QUERY_KEY = ['profile', 'two-factor'];

function useInvalidateTwoFactorStatus() {
	const client = useQueryClient();
	return () => client.invalidateQueries({ queryKey: QUERY_KEY });
}

export function useTwoFactorStatus() {
	return useQuery({ queryKey: QUERY_KEY, queryFn: () => twoFactorService.getStatus() });
}

export function useTwoFactorSetup() {
	const invalidate = useInvalidateTwoFactorStatus();
	const setup = useMutation({ mutationFn: () => twoFactorService.setup() });
	const enable = useMutation({ mutationFn: (code: string) => twoFactorService.enable(code) });

	const reset = () => {
		setup.reset();
		enable.reset();
	};

	return {
		start: () => setup.mutate(),
		isStarting: setup.isPending,
		setup: setup.data,
		confirm: (code: string) => enable.mutate(code),
		isConfirming: enable.isPending,
		recoveryCodes: enable.data,
		cancel: reset,
		finish: async () => {
			await invalidate();
			reset();
		},
	};
}

export function useDisableTwoFactor() {
	const { t } = useTranslation();
	const invalidate = useInvalidateTwoFactorStatus();

	const mutation = useMutation({
		mutationFn: ({ password, code }: { password: string; code: string }) => twoFactorService.disable(password, code),
		onSuccess: async () => {
			toast.success(t('two_factor.alerts.disabled'));
			await invalidate();
		},
	});

	return { disable: mutation.mutate, isLoading: mutation.isPending };
}

export function useRegenerateRecoveryCodes() {
	const invalidate = useInvalidateTwoFactorStatus();

	const mutation = useMutation({
		mutationFn: (code: string) => twoFactorService.regenerateRecoveryCodes(code),
		onSuccess: () => invalidate(),
	});

	return {
		regenerate: mutation.mutate,
		isLoading: mutation.isPending,
		recoveryCodes: mutation.data,
		clear: mutation.reset,
	};
}
