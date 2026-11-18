import { ResolvedStatusDomainSchema } from '@/features/status-pages/schemas/status-page.schema.ts';

const RESOLVE_TIMEOUT_MS = 3000;

/**
 * Asks the API whether the current host is the custom domain of a status page.
 *
 * Runs before the router mounts. Any failure (404 on the main domain, network error, timeout) means "no": the normal
 * app starts, so a hiccup never locks an administrator out of the instance.
 */
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
