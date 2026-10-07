import type { ViewMode } from '@/components/molecules/view-mode-toggle.tsx';
import type { StatusPageLayoutValue } from '@/features/status-pages/schemas/status-page.schema.ts';

export type StatusLayout = ViewMode;

const STORAGE_PREFIX = 'uptime-kotlin.status-layout';

function storageKey(scope?: string) {
	return scope ? `${STORAGE_PREFIX}.${scope}` : STORAGE_PREFIX;
}

/* Visitor's own choice, per page: the admin's default only applies until the visitor picks. */
export function readStoredLayout(scope?: string): StatusLayout | null {
	try {
		const value = localStorage.getItem(storageKey(scope));
		return value === 'grid' || value === 'list' ? value : null;
	} catch {
		return null;
	}
}

export function storeLayout(layout: StatusLayout, scope?: string) {
	try {
		localStorage.setItem(storageKey(scope), layout);
	} catch {
		// Private browsing: the choice lasts for the session only.
	}
}

export const fromApiLayout = (value: StatusPageLayoutValue): StatusLayout => (value === 'LIST' ? 'list' : 'grid');

export const toApiLayout = (value: StatusLayout): StatusPageLayoutValue => (value === 'list' ? 'LIST' : 'GRID');
