// src/api/services/authService.ts

import { auth } from '@/features/auth/enums/auth-enum.ts';
import jwtDecode from '@/lib/jwt-decode.ts';

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

	getUser(): User {
		const user = localStorage.getItem('user');

		if (!user) {
			window.location.href = '/login';
			throw new Error('User not found. Please login again.');
		}

		return JSON.parse(user);
	},

	saveTokens(access: string, refresh: string) {
		localStorage.setItem(auth.TOKEN, access);
		localStorage.setItem(auth.REFRESH_TOKEN, refresh);
	},

	saveUser(email: string, username: string) {
		localStorage.setItem('user', JSON.stringify({ username, email }));
	},

	logout() {
		localStorage.removeItem(auth.TOKEN);
		localStorage.removeItem(auth.REFRESH_TOKEN);
	},

	async tryRefreshToken(): Promise<boolean> {
		const refresh = this.getRefreshToken();
		if (!refresh) return false;

		try {
			const baseUrl = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

			const res = await fetch(`${baseUrl}/auth/refresh`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ refresh_token: refresh }),
			});

			if (!res.ok) return false;

			const data = await res.json();
			this.saveUser(jwtDecode.parseJwt(data.token).email, jwtDecode.parseJwt(data.token).name);
			this.saveTokens(data.token, data.refresh_token);

			return true;
		} catch {
			return false;
		}
	},

	async login(username: string, password: string): Promise<void> {
		const baseUrl = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

		const res = await fetch(`${baseUrl}/auth/login`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ email: username, password }),
		});

		const data = await res.json();
		this.saveUser(jwtDecode.parseJwt(data.token).email, jwtDecode.parseJwt(data.token).name);
		this.saveTokens(data.token, data.refresh_token);
	},
};
export default authService;
