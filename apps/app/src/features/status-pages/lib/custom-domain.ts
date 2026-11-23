import { ResolvedStatusDomainSchema } from '@/features/status-pages/schemas/status-page.schema.ts';

const RESOLVE_TIMEOUT_MS = 3000;

export async function resolveCustomDomainSlug(): Promise<string | null> {
	try {
		const response = await fetch('/api/status-domains/resolve', {
			headers: { Accept: 'application/json' },
			signal: AbortSignal.timeout(RESOLVE_TIMEOUT_MS),
		});
		if (!response.ok) return null;

		const parsed = ResolvedStatusDomainSchema.safeParse(await response.json());
		return parsed.success ? parsed.data.slug : null;
	} catch {
		return null;
	}
}
