const jwtDecoder = {
	parseJwt(token: string) {
		const base64Url = token.split('.')[1];
		const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
		const bytes = Uint8Array.from(window.atob(base64), (char) => char.charCodeAt(0));
		return JSON.parse(new TextDecoder().decode(bytes));
	},

	isExpired(token: string) {
		const decoded = jwtDecoder.parseJwt(token);
		return decoded.exp < Date.now() / 1000;
	},
};

export default jwtDecoder;
