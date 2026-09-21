const CHUNK_ERROR_PATTERNS = [
	/failed to fetch dynamically imported module/i,
	/error loading dynamically imported module/i,
	/importing a module script failed/i,
	/unable to preload css/i,
];

const RELOAD_STORAGE_KEY = 'uptime-kotlin.chunk-reload-at';
const RELOAD_GUARD_MS = 10_000;

let reloadRequested = false;

export function isChunkLoadError(error: unknown): boolean {
	const message = error instanceof Error ? error.message : typeof error === 'string' ? error : '';
	return CHUNK_ERROR_PATTERNS.some((pattern) => pattern.test(message));
}

export function reloadOnce(): boolean {
	if (reloadRequested) return true;

	const now = Date.now();
	try {
		const last = Number(sessionStorage.getItem(RELOAD_STORAGE_KEY));
		if (last && now - last < RELOAD_GUARD_MS) return false;
		sessionStorage.setItem(RELOAD_STORAGE_KEY, String(now));
	} catch {
		return false;
	}

	reloadRequested = true;
	window.location.reload();
	return true;
}
