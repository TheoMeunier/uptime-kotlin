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
	log_retention_days?: number | null;
}

export const SLUG_PATTERN = /^[a-z0-9]+(-[a-z0-9]+)*$/;
export const DOMAIN_PATTERN = /^(?=.{1,253}$)([a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,63}$/;

export function cleanDomain(value: string) {
	return value
		.trim()
		.toLowerCase()
		.replace(/^[a-z][a-z0-9+.-]*:\/\//, '')
		.replace(/[/?#].*$/, '')
		.replace(/:\d*$/, '')
		.replace(/\.$/, '');
}

let groupKeySeq = 0;

export function newGroupKey() {
	groupKeySeq += 1;
	return `group-${Date.now().toString(36)}-${groupKeySeq}`;
}

export function emptyGroup(): EditorGroup {
	return { key: newGroupKey(), name: '', probeIds: [] };
}

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
