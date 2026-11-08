import { z } from 'zod';

const email = z.email();

/** Découpe une liste d'adresses séparées par des virgules ou points-virgules. */
export function parseEmailList(value: string | null | undefined): string[] {
	if (!value) return [];
	return value
		.split(/[,;]/)
		.map((part) => part.trim())
		.filter((part) => part.length > 0);
}

export function isValidEmailList(value: string | null | undefined): boolean {
	const addresses = parseEmailList(value);
	return addresses.length > 0 && addresses.every((address) => email.safeParse(address).success);
}
