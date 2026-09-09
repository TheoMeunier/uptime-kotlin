// src/api/services/authService.ts

import { auth } from '@/features/auth/enums/auth-enum.ts';
import jwtDecode from '@/lib/jwt-decode.ts';
import { createApiError } from '@/api/api-error.ts';

interface User {
	username: string;
	email: string;
	id: number;
}

const authService = {
	getAccessToken() {
		return localStorage.getItem(auth.TOKEN);
	},

	getRefreshToken() {
		return localStorage.getItem(auth.REFRESH_TOKEN);
	},

	getSessionId() {
		return localStorage.getItem(auth.SESSION_ID);
	},

	getUser(): User {
		const user = localStorage.getItem('user');

		if (!user) {
			window.location.href = '/login';
			throw new Error('User not found. Please login again.');
		}

		return JSON.parse(user);
	},

	saveTokens(access: string, refresh: string, sessionId?: string) {
		localStorage.setItem(auth.TOKEN, access);
		localStorage.setItem(auth.REFRESH_TOKEN, refresh);

		if (sessionId) {
			localStorage.setItem(auth.SESSION_ID, sessionId);
		}
	},

	saveUser(email: string, username: string) {
		localStorage.setItem('user', JSON.stringify({ username, email }));
	},

	/**
	 * Drops the local credentials only. Used on paths where the refresh token is already known to be
	 * rejected by the server, so there is nothing left to revoke.
	 */
	clearSession() {
		localStorage.removeItem(auth.TOKEN);
		localStorage.removeItem(auth.REFRESH_TOKEN);
		localStorage.removeItem(auth.SESSION_ID);
		localStorage.removeItem('user');
	},

	/**
	 * Revokes the refresh token server side before clearing the local credentials, so the session
	 * cannot be resumed from a copy of the token. The local state is cleared even when the call
	 * fails: the user asked to be logged out of this browser either way.
	 */
	async logout(): Promise<void> {
		const refresh = this.getRefreshToken();

		try {
			if (refresh) {
				await fetch(`/api/auth/logout`, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({ refresh_token: refresh }),
				});
			}
		} catch {
			// Offline or unreachable API: fall through and clear the browser anyway.
		} finally {
			this.clearSession();
		}
	},

	async tryRefreshToken(): Promise<boolean> {
		const refresh = this.getRefreshToken();
		if (!refresh) {
			window.location.href = '/login';
			return false;
		}

		try {
			const res = await fetch(`/api/auth/refresh`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ refresh_token: refresh }),
			});

			if (!res.ok) {
				const error = await createApiError(res, 'Unable to refresh your session.');
				this.clearSession();
				window.location.href = '/login';
				throw error;
			}

			const data = await res.json();
			this.saveUser(jwtDecode.parseJwt(data.token).email, jwtDecode.parseJwt(data.token).name);
			this.saveTokens(data.token, data.refresh_token, data.session_id);

			return true;
		} catch {
			this.clearSession();
			window.location.href = '/login';
			return false;
		}
	},

	async login(username: string, password: string): Promise<void> {
		const res = await fetch(`/api/auth/login`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ email: username, password }),
		});

		if (!res.ok) {
			throw await createApiError(res, 'Unable to login.');
		}

		const data = await res.json();
		this.saveUser(jwtDecode.parseJwt(data.token).email, jwtDecode.parseJwt(data.token).name);
		this.saveTokens(data.token, data.refresh_token, data.session_id);
	},
};
export default authService;
