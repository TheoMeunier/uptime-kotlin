export interface EditorGroup {
	key: string;
	name: string;
	probeIds: string[];
}

export interface EditorProbe {
	id: string;
	name: string;
	status: string;
	detail?: string | null;
}

export const SLUG_PATTERN = /^[a-z0-9]+(-[a-z0-9]+)*$/;

export function newGroupKey() {
	return crypto.randomUUID();
}

export function emptyGroup(): EditorGroup {
	return { key: newGroupKey(), name: '', probeIds: [] };
}

/* « Mon Entreprise ! » → « mon-entreprise »: what the backend accepts as a slug. */
export function slugify(value: string) {
	return value
		.normalize('NFD')
		.replace(/[̀-ͯ]/g, '')
		.toLowerCase()
		.replace(/[^a-z0-9]+/g, '-')
		.replace(/^-+|-+$/g, '')
		.slice(0, 64)
		.replace(/-+$/g, '');
}
