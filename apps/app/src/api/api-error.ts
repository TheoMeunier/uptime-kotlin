export type ApiViolation = {
	field: string;
	message: string;
};

export class ApiError extends Error {
	constructor(
		message: string,
		public readonly status?: number,
		public readonly details?: unknown,
		public readonly violations: ApiViolation[] = []
	) {
		super(message);
		this.name = 'ApiError';
	}
}

type ApiErrorPayload = {
	message?: string;
	error?: string;
	errors?: unknown;
	title?: string;
	violations?: unknown;
};

function readViolations(payload: ApiErrorPayload | undefined): ApiViolation[] {
	if (!Array.isArray(payload?.violations)) return [];

	return payload.violations.filter(
		(violation): violation is ApiViolation =>
			typeof violation?.message === 'string' && typeof violation?.field === 'string'
	);
}

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
	const violations = readViolations(payload);
	const violationMessage = violations.map((violation) => violation.message).join(' · ');
	const message =
		payload?.message || payload?.error || violationMessage || payload?.title || response.statusText || fallbackMessage;

	return new ApiError(message, response.status, payload?.errors ?? payload, violations);
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
