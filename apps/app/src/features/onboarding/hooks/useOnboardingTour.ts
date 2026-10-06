import { useCallback, useEffect, useRef, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import onboardingService, { type OnboardingStatus } from '@/features/onboarding/services/onboardingService.ts';

const STORAGE_PREFIX = 'uptime-kotlin.onboarding.completed.';

function readLocal(email: string): boolean {
	try {
		return localStorage.getItem(STORAGE_PREFIX + email) === 'true';
	} catch {
		return false;
	}
}

function writeLocal(email: string) {
	try {
		localStorage.setItem(STORAGE_PREFIX + email, 'true');
	} catch {
		// Storage is probably full, ignore.
	}
}

export default function useOnboardingTour(email: string) {
	const queryClient = useQueryClient();
	const queryKey = ['onboarding', email];

	const [manualOpen, setManualOpen] = useState(false);
	const [dismissed, setDismissed] = useState(false);
	const locallyCompleted = readLocal(email);

	const { data } = useQuery({
		queryKey,
		queryFn: () => onboardingService.getStatus(),
		staleTime: Infinity,
		retry: false,
		meta: { silentError: true },
	});

	const syncing = useRef(false);

	const completeOnServer = useCallback(() => {
		if (syncing.current) return;
		syncing.current = true;

		onboardingService
			.complete()
			.then((status) => queryClient.setQueryData<OnboardingStatus>(['onboarding', email], status))
			.catch(() => {
				// Silencieux : le navigateur s'en souvient, la synchro sera retentée au prochain chargement.
			})
			.finally(() => {
				syncing.current = false;
			});
	}, [email, queryClient]);

	const serverCompleted = data?.completed;

	useEffect(() => {
		if (serverCompleted === true && !locallyCompleted) writeLocal(email);
		if (serverCompleted === false && locallyCompleted) completeOnServer();
	}, [serverCompleted, locallyCompleted, email, completeOnServer]);

	const autoOpen = serverCompleted === false && !locallyCompleted && !dismissed;

	const start = useCallback(() => setManualOpen(true), []);

	const close = useCallback(() => {
		setManualOpen(false);
		setDismissed(true);

		writeLocal(email);
	}, [email]);

	return { open: manualOpen || autoOpen, start, close };
}
