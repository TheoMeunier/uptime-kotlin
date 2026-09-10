const formatters = new Map<string, Intl.DateTimeFormat>();

function formatter(locale: string, options: Intl.DateTimeFormatOptions): Intl.DateTimeFormat {
	const key = `${locale}|${JSON.stringify(options)}`;
	let cached = formatters.get(key);

	if (!cached) {
		cached = new Intl.DateTimeFormat(locale, options);
		formatters.set(key, cached);
	}

	return cached;
}

function toDate(value: Date | string | number): Date | null {
	const date = value instanceof Date ? value : new Date(value);
	return Number.isNaN(date.getTime()) ? null : date;
}

export function formatTime(
	value: Date | string | number,
	locale: string,
	{ withSeconds = false }: { withSeconds?: boolean } = {}
): string {
	const date = toDate(value);
	if (!date) return '';

	return formatter(locale, {
		hour: '2-digit',
		minute: '2-digit',
		...(withSeconds ? { second: '2-digit' as const } : {}),
	}).format(date);
}

export function formatDayShort(value: Date | string | number, locale: string): string {
	const date = toDate(value);
	if (!date) return '';

	return formatter(locale, { month: 'short', day: 'numeric' }).format(date);
}

export function formatDayLong(value: Date | string | number, locale: string): string {
	const date = toDate(value);
	if (!date) return '';

	return formatter(locale, { day: 'numeric', month: 'long', year: 'numeric' }).format(date);
}

export function formatShortDateTime(value: Date | string | number, locale: string): string {
	const date = toDate(value);
	if (!date) return '';

	return formatter(locale, {
		day: '2-digit',
		month: '2-digit',
		hour: '2-digit',
		minute: '2-digit',
	}).format(date);
}

export function formatDateTime(value: Date | string | number, locale: string, timezone?: string): string {
	const date = toDate(value);
	if (!date) return '';

	return formatter(locale, {
		dateStyle: 'medium',
		timeStyle: 'short',
		...(timezone ? { timeZone: timezone } : {}),
	}).format(date);
}

export function formatDuration(totalSeconds: number): string {
	const days = Math.floor(totalSeconds / 86400);
	const hours = Math.floor((totalSeconds % 86400) / 3600);
	const minutes = Math.floor((totalSeconds % 3600) / 60);

	const parts: string[] = [];
	if (days > 0) parts.push(`${days}d`);
	if (hours > 0) parts.push(`${hours}h`);
	if (minutes > 0 && days === 0) parts.push(`${minutes}m`);

	return parts.join(' ') || '0m';
}

export function formatRelativeDuration(value: Date | string | number): string {
	const date = toDate(value);
	if (!date) return '';

	return formatDuration(Math.max(0, Math.round(Math.abs(date.getTime() - Date.now()) / 1000)));
}

export function toDatetimeLocal(date: Date): string {
	const pad = (value: number) => String(value).padStart(2, '0');

	return (
		`${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
		`T${pad(date.getHours())}:${pad(date.getMinutes())}`
	);
}

export function toDatetimeLocalInZone(value: Date | string | number, timezone: string): string {
	const date = toDate(value);
	if (!date) return '';

	const parts = formatter('en-CA', {
		timeZone: timezone,
		year: 'numeric',
		month: '2-digit',
		day: '2-digit',
		hour: '2-digit',
		minute: '2-digit',
		hourCycle: 'h23',
	}).formatToParts(date);

	const get = (type: string) => parts.find((part) => part.type === type)?.value ?? '00';

	return `${get('year')}-${get('month')}-${get('day')}T${get('hour')}:${get('minute')}`;
}

function zoneOffsetMs(at: Date, timezone: string): number {
	const asZoned = new Date(at.toLocaleString('en-US', { timeZone: timezone }));
	const asUtc = new Date(at.toLocaleString('en-US', { timeZone: 'UTC' }));

	return asZoned.getTime() - asUtc.getTime();
}

export function zonedWallClockToInstant(localValue: string, timezone: string): string {
	const naive = new Date(`${localValue}:00Z`);
	if (Number.isNaN(naive.getTime())) return new Date().toISOString();

	const firstGuess = new Date(naive.getTime() - zoneOffsetMs(naive, timezone));

	return new Date(naive.getTime() - zoneOffsetMs(firstGuess, timezone)).toISOString();
}

const FALLBACK_ZONES = [
	'UTC',
	'Europe/Paris',
	'Europe/London',
	'Europe/Berlin',
	'Europe/Madrid',
	'Europe/Lisbon',
	'America/New_York',
	'America/Chicago',
	'America/Denver',
	'America/Los_Angeles',
	'America/Sao_Paulo',
	'Asia/Dubai',
	'Asia/Kolkata',
	'Asia/Singapore',
	'Asia/Tokyo',
	'Australia/Sydney',
];

export function timezoneOptions(): string[] {
	const intl = Intl as typeof Intl & { supportedValuesOf?: (key: string) => string[] };
	const zones = typeof intl.supportedValuesOf === 'function' ? intl.supportedValuesOf('timeZone') : [...FALLBACK_ZONES];

	return [...new Set(['UTC', Intl.DateTimeFormat().resolvedOptions().timeZone, ...zones].filter(Boolean))];
}
