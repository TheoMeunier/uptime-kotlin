const DEFAULT_TARGET = '/dashboard';

export const CHANGE_PASSWORD_PATH = '/change-password';

export function safeRedirect(target: string | null | undefined, fallback = DEFAULT_TARGET): string {
	if (!target || !target.startsWith('/') || target.startsWith('//') || target.startsWith('/\\')) return fallback;
	if (target === '/' || target === '/login' || target.startsWith('/login?')) return fallback;
	return target;
}

export function loginPath(target?: string): string {
	const back = safeRedirect(target, '');
	return back ? `/login?redirect=${encodeURIComponent(back)}` : '/login';
}

export function changePasswordPath(target?: string): string {
	const back = safeRedirect(target, '');
	return back && !back.startsWith(CHANGE_PASSWORD_PATH)
		? `${CHANGE_PASSWORD_PATH}?redirect=${encodeURIComponent(back)}`
		: CHANGE_PASSWORD_PATH;
}

export function loginPathFromHere(): string {
	return loginPath(window.location.pathname + window.location.search);
}
