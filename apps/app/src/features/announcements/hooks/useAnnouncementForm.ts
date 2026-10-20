import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import i18n from '@/lang/i18n.ts';
import { toDatetimeLocal } from '@/lib/datetime.ts';
import { AnnouncementSeverityEnum, type Announcement } from '@/features/announcements/schemas/announcement.schema.ts';

export const ANNOUNCEMENT_BODY_MAX = 5000;

export const announcementFormSchema = z
	.object({
		title: z.string().trim().min(1).max(255),
		body: z.string().max(ANNOUNCEMENT_BODY_MAX).optional(),
		severity: z.enum(AnnouncementSeverityEnum),
		starts_at: z.string().min(1),
		ends_at: z.string().optional(),
		show_on_global: z.boolean(),
		status_page_ids: z.array(z.uuid()),
	})
	.refine((values) => !values.ends_at || values.ends_at > values.starts_at, {
		error: () => i18n.t('announcements.validation.ends_after_start'),
		path: ['ends_at'],
	})
	.refine((values) => values.show_on_global || values.status_page_ids.length > 0, {
		error: () => i18n.t('announcements.validation.target'),
		path: ['status_page_ids'],
	});

export type AnnouncementFormValues = z.infer<typeof announcementFormSchema>;

/** Form values of an existing announcement, its dates in the browser's zone. */
export function toFormValues(announcement: Announcement): AnnouncementFormValues {
	return {
		title: announcement.title,
		body: announcement.body ?? '',
		severity: announcement.severity,
		starts_at: toDatetimeLocal(new Date(announcement.starts_at)),
		ends_at: announcement.ends_at ? toDatetimeLocal(new Date(announcement.ends_at)) : '',
		show_on_global: announcement.show_on_global,
		status_page_ids: announcement.pages.map((page) => page.id),
	};
}

export default function useAnnouncementForm(defaultValues: Partial<AnnouncementFormValues> = {}) {
	const form = useForm<AnnouncementFormValues>({
		resolver: zodResolver(announcementFormSchema),
		defaultValues: {
			title: '',
			body: '',
			severity: 'INFO',
			starts_at: toDatetimeLocal(new Date()),
			ends_at: '',
			show_on_global: false,
			status_page_ids: [],
			...defaultValues,
		},
	});

	return { form, errors: form.formState.errors };
}
