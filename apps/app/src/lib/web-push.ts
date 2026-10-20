export interface WebPushSubscriptionKeys {
	endpoint: string;
	p256dh: string;
	auth: string;
}

export type WebPushSupport = 'supported' | 'unsupported' | 'insecure';

const SERVICE_WORKER_URL = '/sw.js';

export function getWebPushSupport(): WebPushSupport {
	if (typeof window === 'undefined') return 'unsupported';
	if (!window.isSecureContext) return 'insecure';
	if (!('serviceWorker' in navigator) || !('PushManager' in window) || !('Notification' in window)) {
		return 'unsupported';
	}
	return 'supported';
}

export function getNotificationPermission(): NotificationPermission | 'unsupported' {
	return 'Notification' in window ? Notification.permission : 'unsupported';
}

function base64UrlToBytes(value: string): Uint8Array<ArrayBuffer> {
	const base64 = value
		.replace(/-/g, '+')
		.replace(/_/g, '/')
		.padEnd(Math.ceil(value.length / 4) * 4, '=');
	const binary = atob(base64);
	const bytes = new Uint8Array(new ArrayBuffer(binary.length));
	for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
	return bytes;
}

function bytesToBase64Url(buffer: ArrayBuffer | null): string {
	if (!buffer) return '';
	let binary = '';
	new Uint8Array(buffer).forEach((byte) => (binary += String.fromCharCode(byte)));
	return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function sameKey(current: ArrayBuffer | null | undefined, expected: Uint8Array): boolean {
	if (!current) return false;
	const bytes = new Uint8Array(current);
	return bytes.length === expected.length && bytes.every((byte, i) => byte === expected[i]);
}

function toKeys(subscription: PushSubscription): WebPushSubscriptionKeys {
	return {
		endpoint: subscription.endpoint,
		p256dh: bytesToBase64Url(subscription.getKey('p256dh')),
		auth: bytesToBase64Url(subscription.getKey('auth')),
	};
}

export async function getCurrentSubscription(publicKey: string): Promise<WebPushSubscriptionKeys | null> {
	if (getWebPushSupport() !== 'supported') return null;
	const registration = await navigator.serviceWorker.getRegistration('/');
	const subscription = await registration?.pushManager.getSubscription();
	if (!subscription || !sameKey(subscription.options.applicationServerKey, base64UrlToBytes(publicKey))) return null;
	return toKeys(subscription);
}

export async function subscribeThisBrowser(publicKey: string): Promise<WebPushSubscriptionKeys> {
	const permission = await Notification.requestPermission();
	if (permission !== 'granted') throw new WebPushPermissionError(permission);

	await navigator.serviceWorker.register(SERVICE_WORKER_URL, { scope: '/' });
	const registration = await navigator.serviceWorker.ready;
	const applicationServerKey = base64UrlToBytes(publicKey);

	const existing = await registration.pushManager.getSubscription();
	if (existing && sameKey(existing.options.applicationServerKey, applicationServerKey)) return toKeys(existing);
	await existing?.unsubscribe();

	const subscription = await registration.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey });
	return toKeys(subscription);
}

export class WebPushPermissionError extends Error {
	constructor(readonly permission: NotificationPermission) {
		super(`Notification permission ${permission}`);
	}
}

export function describeThisBrowser(userAgent = navigator.userAgent): string {
	const browser = /Edg\//.test(userAgent)
		? 'Edge'
		: /OPR\//.test(userAgent)
			? 'Opera'
			: /Firefox\//.test(userAgent)
				? 'Firefox'
				: /Chrome\//.test(userAgent)
					? 'Chrome'
					: /Safari\//.test(userAgent)
						? 'Safari'
						: 'Browser';
	const system = /iPhone|iPad/.test(userAgent)
		? 'iOS'
		: /Android/.test(userAgent)
			? 'Android'
			: /Mac OS X|Macintosh/.test(userAgent)
				? 'macOS'
				: /Windows/.test(userAgent)
					? 'Windows'
					: /Linux/.test(userAgent)
						? 'Linux'
						: '';
	return system ? `${browser} · ${system}` : browser;
}
