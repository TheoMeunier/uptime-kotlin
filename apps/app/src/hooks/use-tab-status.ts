import { useEffect } from 'react';
import { applyFaviconBadge } from '@/lib/favicon.ts';
import type { AttentionSummary } from '@/lib/status.ts';

export const APP_NAME = 'Uptime Kotlin';

interface TabStatusOptions extends AttentionSummary {
	page?: string;
}

export default function useTabStatus({ count, severity, page }: TabStatusOptions) {
	useEffect(() => {
		const prefix = count > 0 ? `(${count}) ` : '';
		document.title = page ? `${prefix}${page} · ${APP_NAME}` : `${prefix}${APP_NAME}`;
	}, [count, page]);

	useEffect(() => {
		void applyFaviconBadge(severity === 'none' ? null : severity);
	}, [severity]);

	useEffect(() => {
		return () => {
			document.title = APP_NAME;
			void applyFaviconBadge(null);
		};
	}, []);
}
