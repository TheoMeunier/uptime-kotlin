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
    console.log("[fetchClient] Auth required:", auth);
    console.log("[fetchClient] Token:", token ? `${token.substring(0, 20)}...` : "null");

    const finalHeaders: HeadersInit = {
        "Content-Type": "application/json",
        ...(headers ?? {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };

    const url = BASE_URL + endpoint + buildQueryParams(params);
    console.log("[fetchClient] Request:", method, url);

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
    console.log("[fetchClient] Starting request to:", endpoint);

    try {
        let response = await rawRequest(endpoint, method, options);
        console.log("[fetchClient] Response status:", response.status);

        if (response.status === 401 && options.auth) {
            console.log("[fetchClient] 401 detected, attempting token refresh...");

            try {
                const refreshed = await authService.tryRefreshToken();
                console.log("[fetchClient] Token refresh result:", refreshed);

                if (refreshed) {
                    console.log("[fetchClient] Retrying request with new token...");
                    response = await rawRequest(endpoint, method, options);
                    console.log("[fetchClient] Retry response status:", response.status);
                } else {
                    console.log("[fetchClient] Token refresh failed, logging out...");
                    authService.logout();
                    throw new Error("Session expirée.");
                }
            } catch (refreshError) {
                console.error("[fetchClient] Error during token refresh:", refreshError);
                authService.logout();
                throw new Error("Session expirée.");
            }
        }

        if (!response.ok) {
            const text = await response.text().catch(() => null);
            console.error("[fetchClient] Request failed:", response.status, text);
            throw new Error(`Erreur API ${response.status}: ${text}`);
        }

        if (response.status === 204) {
            console.log("[fetchClient] 204 No Content - returning empty object");
            return {} as T;
        }

        const data = await response.json();
        console.log("[fetchClient] Request successful, data received");
        return data as T;
    } catch (error) {
        console.error("[fetchClient] Request error:", error);
        throw error;
    }
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