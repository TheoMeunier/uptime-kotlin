import { createContext, type ReactNode } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import setupService from '@/features/setup/services/setupService.ts';

interface SetupContextType {
	isSetupComplete: boolean;
	isLoading: boolean;
	updateSetupStatus: (status: boolean) => void;
}

const SetupContext = createContext<SetupContextType | null>(null);

export default SetupContext;

const SETUP_STATUS_QUERY_KEY = ['app-status'] as const;

async function fetchSetupStatus(): Promise<boolean> {
	const data = await setupService.getIsFistStartApplication();
	return data?.status === true || data?.status === 'true';
}

export function SetupProvider({ children }: { children: ReactNode }) {
	const queryClient = useQueryClient();

	const { data, isPending } = useQuery({
		queryKey: SETUP_STATUS_QUERY_KEY,
		queryFn: fetchSetupStatus,
		staleTime: Infinity,
		retry: false,
		meta: { silentError: true },
	});

	const updateSetupStatus = (status: boolean) => {
		queryClient.setQueryData(SETUP_STATUS_QUERY_KEY, status);
	};

	return (
		<SetupContext.Provider
			value={{
				isSetupComplete: data ?? false,
				isLoading: isPending,
				updateSetupStatus,
			}}
		>
			{children}
		</SetupContext.Provider>
	);
}
