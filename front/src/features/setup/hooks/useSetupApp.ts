import { useContext } from 'react';
import SetupContext from '@/features/setup/contexts/setup-context.tsx';

export default function useSetup() {
	const context = useContext(SetupContext);
	if (!context) {
		throw new Error('useSetup must be used within SetupProvider');
	}
	return context;
}
