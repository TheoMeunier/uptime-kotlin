import { useState } from 'react';
import { readStoredLayout, type StatusLayout, storeLayout } from '@/features/status-pages/lib/status-layout.ts';

export default function useStatusLayout(fallback: StatusLayout, scope?: string) {
	const [chosen, setChosen] = useState<StatusLayout | null>(() => readStoredLayout(scope));

	const setLayout = (layout: StatusLayout) => {
		setChosen(layout);
		storeLayout(layout, scope);
	};

	return [chosen ?? fallback, setLayout] as const;
}
