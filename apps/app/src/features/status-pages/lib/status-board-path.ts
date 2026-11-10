const STATUS_BOARD_PATH = /^\/status(?:\/([^/]+))?\/?$/;

export function isStatusBoardPath(pathname: string): boolean {
	return STATUS_BOARD_PATH.test(pathname);
}

export function statusSlugFromPath(pathname: string): string | undefined {
	const raw = STATUS_BOARD_PATH.exec(pathname)?.[1];
	if (!raw) return undefined;
	try {
		return decodeURIComponent(raw);
	} catch {
		return raw;
	}
}
