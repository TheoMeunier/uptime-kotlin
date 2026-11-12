import ky from 'ky';
import authService from '@/features/auth/services/authServices.ts';
import { ApiError, createApiError, registerErrorMessage } from '@/api/api-error.ts';
import { CHANGE_PASSWORD_PATH, changePasswordPath, loginPathFromHere } from '@/features/auth/lib/redirect.ts';
import i18n from '@/lang/i18n.ts';

registerErrorMessage('PASSWORD_CHANGE_REQUIRED', () => i18n.t('pages.change_password.required'));

async function readErrorCode(response: Response): Promise<string | undefined> {
	try {
		const payload = await response.clone().json();
		return typeof payload?.error === 'string' ? payload.error : undefined;
	} catch {
		return undefined;
	}
}

function onPasswordChangeRequired() {
	if (window.location.pathname === CHANGE_PASSWORD_PATH) return;

	if (authService.mustChangePassword()) {
		window.location.href = changePasswordPath(window.location.pathname + window.location.search);
		return;
	}

	authService.clearSession();
	window.location.href = loginPathFromHere();
}

let refreshTokenRequest: Promise<boolean> | null = null;

function refreshAccessToken() {
	refreshTokenRequest ??= authService.tryRefreshToken().finally(() => {
		refreshTokenRequest = null;
	});

	return refreshTokenRequest;
}

const api = ky.extend({
	prefix: '/api',
	timeout: 30000,
	parseJson: (text) => (text === '' ? undefined : JSON.parse(text)),
	headers: {
		'Content-Type': 'application/json',
	},
	hooks: {
		beforeRequest: [
			({ request }) => {
				const token = authService.getAccessToken();

				if (token) {
					request.headers.set('Authorization', `Bearer ${token}`);
				}
			},
		],
		afterResponse: [
			async ({ request, response, retryCount }) => {
				if (response.status === 401 && retryCount === 0) {
					const refreshed = await refreshAccessToken();

					if (!refreshed) {
						throw new ApiError('Your session has expired. Please login again.', 401);
					}

					const token = authService.getAccessToken();
					const headers = new Headers(request.headers);

					if (token) {
						headers.set('Authorization', `Bearer ${token}`);
					}

					return ky.retry({
						request: new Request(request, { headers }),
						code: 'TOKEN_REFRESHED',
					});
				}

				if (response.status === 403 && (await readErrorCode(response)) === 'PASSWORD_CHANGE_REQUIRED') {
					onPasswordChangeRequired();
				}

				if (!response.ok) {
					throw await createApiError(response);
				}

				return response;
			},
		],
	},
	retry: {
		methods: ['get', 'post'],
		limit: 2,
		statusCodes: [408, 413, 429, 500, 502, 503, 504],
	},
});

export default api;
