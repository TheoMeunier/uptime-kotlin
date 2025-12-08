// src/api/services/authService.ts

import {auth} from "@/features/auth/enums/auth-enum.ts";

const authService = {
    getAccessToken() {
        return localStorage.getItem(auth.TOKEN);
    },

    getRefreshToken() {
        return localStorage.getItem(auth.REFRESH_TOKEN);
    },

    saveTokens(access: string, refresh: string) {
        localStorage.setItem(auth.TOKEN, access);
        localStorage.setItem(auth.REFRESH_TOKEN, refresh);
    },

    logout() {
        localStorage.removeItem(auth.TOKEN);
        localStorage.removeItem(auth.REFRESH_TOKEN);
        window.location.href = "/login";
    },

    async tryRefreshToken(): Promise<boolean> {
        const refresh = this.getRefreshToken();
        if (!refresh) return false;

        try {
            const res = await fetch("/auth/refresh", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ refreshToken: refresh }),
            });

            if (!res.ok) return false;

            const data = await res.json();
            this.saveTokens(data.accessToken, data.refreshToken);

            return true;
        } catch {
            return false;
        }
    },

    async login(username: string, password: string): Promise<void> {
        const baseUrl = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

        const res = await fetch(`${baseUrl}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: username, password }),
        });

        const data = await res.json();
        this.saveTokens(data.token, data.refresh_token);
    }
};
export default authService
