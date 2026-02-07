import { createContext, type ReactNode, useEffect, useState } from 'react';
import setupService from '@/features/setup/services/setupService.ts';

interface SetupContextType {
	isSetupComplete: boolean;
	isLoading: boolean;
	updateSetupStatus: (status: boolean) => void;
}

const SetupContext = createContext<SetupContextType | null>(null);

export default SetupContext;

export function SetupProvider({ children }: { children: ReactNode }) {
	const [isSetupComplete, setIsSetupComplete] = useState<boolean | null>(() => {
		const cached = sessionStorage.getItem('setupCompleted');
		return cached !== null ? cached === 'true' : null;
	});
	const [isLoading, setIsLoading] = useState<boolean>(() => {
		return sessionStorage.getItem('setupCompleted') === null;
	});

	useEffect(() => {
		if (isSetupComplete !== null) {
			return;
		}

		setupService
			.getIsFistStartApplication()
			.then((data) => {
				const completed = data.status;
				setIsSetupComplete(completed);
				sessionStorage.setItem('setupCompleted', String(completed));
			})
			.catch((error) => {
				console.error('Error checking setup status:', error);
				setIsSetupComplete(false);
			})
			.finally(() => {
				setIsLoading(false);
			});
	}, [isSetupComplete]);

	const updateSetupStatus = (status: boolean) => {
		setIsSetupComplete(status);
		sessionStorage.setItem('setupCompleted', String(status));
	};

	return (
		<SetupContext.Provider
			value={{
				isSetupComplete: isSetupComplete ?? false,
				isLoading,
				updateSetupStatus,
			}}
		>
			{children}
		</SetupContext.Provider>
	);
}
