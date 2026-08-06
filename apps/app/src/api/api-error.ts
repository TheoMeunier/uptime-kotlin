export class ApiError extends Error {
	constructor(
		message: string,
		public readonly status?: number,
		public readonly details?: unknown
	) {
		super(message);
		this.name = 'ApiError';
	}
}

type ApiErrorPayload = {
	message?: string;
	error?: string;
	errors?: unknown;
};

export function getApiErrorMessage(error: unknown) {
	if (error instanceof ApiError) {
		return error.message;
	}

	if (error instanceof Error) {
		return error.message;
	}

	return 'An unexpected error occurred.';
}

export async function createApiError(response: Response, fallbackMessage = 'An API error occurred.') {
	const payload = await readErrorPayload(response);
	const message = payload?.message || payload?.error || response.statusText || fallbackMessage;

	return new ApiError(message, response.status, payload?.errors ?? payload);
}

async function readErrorPayload(response: Response): Promise<ApiErrorPayload | undefined> {
	try {
		const contentType = response.headers.get('content-type');

		if (contentType?.includes('application/json')) {
			return (await response.clone().json()) as ApiErrorPayload;
		}

		const text = await response.clone().text();
		return text ? { message: text } : undefined;
	} catch {
		return undefined;
	}
}
