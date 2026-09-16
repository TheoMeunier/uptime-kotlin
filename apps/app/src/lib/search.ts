export function normalizeForSearch(value: string): string {
	return value
		.normalize('NFD')
		.replace(/\p{Diacritic}/gu, '')
		.toLowerCase();
}

export function createSearchMatcher(query: string): (...fields: Array<string | null | undefined>) => boolean {
	const terms = normalizeForSearch(query).split(/\s+/).filter(Boolean);

	if (terms.length === 0) return () => true;

	return (...fields) => {
		const haystack = fields
			.filter((field): field is string => Boolean(field))
			.map(normalizeForSearch)
			.join(' ');

		return terms.every((term) => haystack.includes(term));
	};
}
