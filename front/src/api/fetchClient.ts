import authService from "@/features/auth/services/authServices.ts";

export type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

export interface RequestOptions extends Omit<RequestInit, "body"> {
    params?: Record<string, string | number | boolean>;
    body?: unknown;
    auth?: boolean;
}

const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

function buildQueryParams(params?: Record<string, any>): string {
    if (!params) return "";
    const query = new URLSearchParams();
    for (const [k, v] of Object.entries(params)) {
        if (v != null) query.append(k, String(v));
    }
    return `?${query.toString()}`;
}

async function rawRequest(
    endpoint: string,
    method: HttpMethod,
    options: RequestOptions = {}
): Promise<Response> {
    const { params, body, auth, headers, ...rest } = options;

    const token = auth ? authService.getAccessToken() : null;

    const finalHeaders: HeadersInit = {
        "Content-Type": "application/json",
        ...(headers ?? {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };

    const url = BASE_URL + endpoint + buildQueryParams(params);

    return fetch(url, {
        method,
        headers: finalHeaders,
        body: body ? JSON.stringify(body) : undefined,
        ...rest,
    });
}

export async function request<T>(
    endpoint: string,
    method: HttpMethod,
    options: RequestOptions = {}
): Promise<T> {
    let response = await rawRequest(endpoint, method, options);

    if (response.status === 401 && options.auth) {
        const refreshed = await authService.tryRefreshToken();

        if (refreshed) {
            response = await rawRequest(endpoint, method, options);
        } else {
            authService.logout();
            throw new Error("Session expirée.");
        }
    }

    if (!response.ok) {
        const text = await response.text().catch(() => null);
        throw new Error(`Erreur API ${response.status}: ${text}`);
    }

    if (response.status === 204) return {} as T;
    return await response.json() as Promise<T>;
}

export const fetchClient = {
    get: <T>(endpoint: string, options?: RequestOptions) =>
request<T>(endpoint, "GET", options ?? {}),

    post: <T>(endpoint: string, body?: unknown, options?: RequestOptions) =>
request<T>(endpoint, "POST", { ...options, body }),

    put: <T>(endpoint: string, body?: unknown, options?: RequestOptions) =>
request<T>(endpoint, "PUT", { ...options, body }),

    patch: <T>(endpoint: string, body?: unknown, options?: RequestOptions) =>
request<T>(endpoint, "PATCH", { ...options, body }),

    delete: <T>(endpoint: string, options?: RequestOptions) =>
request<T>(endpoint, "DELETE", options ?? {}),
};
