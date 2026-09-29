import ky from 'ky';
import authService from '@/features/auth/services/authServices.ts';
import { ApiError, createApiError } from '@/api/api-error.ts';

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
