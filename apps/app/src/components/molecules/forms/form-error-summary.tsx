import type { FieldErrors, FieldValues } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { CircleAlert } from 'lucide-react';

type SummaryEntry = { path: string; message: string };

function collectErrors(errors: unknown, path: string[] = []): SummaryEntry[] {
	if (!errors || typeof errors !== 'object') return [];

	const node = errors as Record<string, unknown>;
	if (typeof node.message === 'string' && node.message) {
		return [{ path: path.join('.'), message: node.message }];
	}

	return Object.entries(node)
		.filter(([key]) => key !== 'ref' && key !== 'type' && key !== 'types')
		.flatMap(([key, child]) => collectErrors(child, [...path, key]));
}

export default function FormErrorSummary<T extends FieldValues>({
	errors,
	labelPrefixes = [],
}: {
	errors: FieldErrors<T>;
	labelPrefixes?: string[];
}) {
	const { t, i18n } = useTranslation();
	const entries = collectErrors(errors);

	if (!entries.length) return null;

	const labelOf = (path: string) => {
		const root = path.split('.')[0];
		const key = labelPrefixes.map((prefix) => `${prefix}${root}`).find((candidate) => i18n.exists(candidate));
		const label = key ? t(key) : root;
		const rest = path.slice(root.length);
		return rest ? `${label}${rest}` : label;
	};

	return (
		<div
			role="alert"
			className="border-destructive/40 bg-destructive/5 text-destructive mt-6 rounded-lg border p-4 text-sm"
		>
			<p className="flex items-center gap-2 font-medium">
				<CircleAlert className="size-4" />
				{t('validation.summary')}
			</p>
			<ul className="mt-2 ml-6 flex list-disc flex-col gap-1">
				{entries.map((entry) => (
					<li key={entry.path || entry.message}>
						{entry.path && <span className="font-medium">{labelOf(entry.path)} — </span>}
						{entry.message}
					</li>
				))}
			</ul>
		</div>
	);
}
