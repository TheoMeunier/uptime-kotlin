import { z } from 'zod';

const LEGACY_SCHEME = /^https?:\/\//i;
const LABEL = /^[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?$/;
const ipv4 = z.ipv4();
const ipv6 = z.ipv6();

export function isValidHostnameOrIp(value: string | null | undefined): boolean {
	if (!value) return false;
	const host = value.trim().replace(LEGACY_SCHEME, '');
	if (!host) return false;

	if (host.includes(':')) {
		return ipv6.safeParse(host.replace(/^\[/, '').replace(/\]$/, '')).success;
	}

	const name = host.replace(/\.$/, '');
	if (!name || name.length > 253) return false;

	const labels = name.split('.');
	if (!labels.every((label) => LABEL.test(label))) return false;

	return /^\d+$/.test(labels[labels.length - 1]) ? ipv4.safeParse(name).success : true;
}
