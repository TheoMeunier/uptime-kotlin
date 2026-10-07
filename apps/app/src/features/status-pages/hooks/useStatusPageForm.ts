import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { z } from 'zod';
import {
	emptyGroup,
	type EditorGroup,
	newGroupKey,
	SLUG_PATTERN,
	slugify,
} from '@/features/status-pages/lib/editor.ts';
import type { StatusPageDetail, StatusPageLayoutValue } from '@/features/status-pages/schemas/status-page.schema.ts';
import type { StatusPagePayload } from '@/features/status-pages/services/status-page-service.ts';

export interface StatusPageFormValues {
	title: string;
	slug: string;
	description: string;
	defaultLayout: StatusPageLayoutValue;
	groups: EditorGroup[];
}

export type StatusPageFormErrors = Partial<Record<'title' | 'slug', string>>;

function initialValues(page?: StatusPageDetail): StatusPageFormValues {
	if (!page) return { title: '', slug: '', description: '', defaultLayout: 'GRID', groups: [emptyGroup()] };

	return {
		title: page.title,
		slug: page.slug,
		description: page.description ?? '',
		defaultLayout: page.default_layout,
		groups: page.groups.map((group) => ({
			key: newGroupKey(),
			name: group.name ?? '',
			probeIds: group.probes.map((probe) => probe.id),
		})),
	};
}

function toPayload(values: StatusPageFormValues): StatusPagePayload {
	return {
		title: values.title.trim(),
		slug: values.slug.trim(),
		description: values.description.trim(),
		default_layout: values.defaultLayout,
		groups: values.groups.map((group) => ({ name: group.name.trim(), probe_ids: group.probeIds })),
	};
}

export default function useStatusPageForm(page: StatusPageDetail | undefined) {
	const { t } = useTranslation();
	const [values, setValues] = useState(() => initialValues(page));
	const [savedPayload, setSavedPayload] = useState(() => JSON.stringify(toPayload(initialValues(page))));
	const [slugEdited, setSlugEdited] = useState(Boolean(page));
	const [submitted, setSubmitted] = useState(false);

	const errors = useMemo<StatusPageFormErrors>(() => {
		const schema = z.object({
			title: z.string().trim().min(1, t('status_pages.validation.title')).max(255),
			slug: z.string().trim().max(64).regex(SLUG_PATTERN, t('status_pages.validation.slug')),
		});
		const result = schema.safeParse(values);
		if (result.success) return {};

		return Object.fromEntries(result.error.issues.map((issue) => [issue.path[0], issue.message]));
	}, [values, t]);

	const payload = toPayload(values);
	const isDirty = JSON.stringify(payload) !== savedPayload;

	const update = (patch: Partial<StatusPageFormValues>) => setValues((current) => ({ ...current, ...patch }));

	return {
		values,
		errors,
		isDirty,
		showErrors: submitted,
		setTitle: (title: string) =>
			setValues((current) => ({ ...current, title, slug: slugEdited ? current.slug : slugify(title) })),
		setSlug: (slug: string) => {
			setSlugEdited(true);
			update({ slug: slug.toLowerCase() });
		},
		setDescription: (description: string) => update({ description }),
		setDefaultLayout: (defaultLayout: StatusPageLayoutValue) => update({ defaultLayout }),
		setGroups: (groups: EditorGroup[]) => update({ groups }),
		handleSubmit:
			<T>(onSubmit: (payload: StatusPagePayload) => Promise<T>, onSaved?: (result: T) => void) =>
			(event: React.FormEvent) => {
				event.preventDefault();
				setSubmitted(true);
				if (Object.keys(errors).length > 0) return;

				const snapshot = JSON.stringify(payload);
				onSubmit(payload)
					.then((result) => {
						setSavedPayload(snapshot);
						onSaved?.(result);
					})
					.catch(() => {});
			},
	};
}
