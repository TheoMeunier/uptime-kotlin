import api from '@/api/kyClient.ts';
import { registerErrorMessage } from '@/api/api-error.ts';
import i18n from '@/lang/i18n.ts';
import {
	AnnouncementListSchema,
	AnnouncementSchema,
	CreatedAnnouncementSchema,
} from '@/features/announcements/schemas/announcement.schema.ts';
import type { AnnouncementFormValues } from '@/features/announcements/hooks/useAnnouncementForm.ts';

registerErrorMessage('ANNOUNCEMENT_NOT_STARTED', () => i18n.t('announcements.alerts.not_started'));

/** `datetime-local` values are wall-clock times in the browser's zone. */
const toInstant = (local: string) => new Date(local).toISOString();

function toBody(values: AnnouncementFormValues) {
	return JSON.stringify({
		title: values.title.trim(),
		body: values.body?.trim() || null,
		severity: values.severity,
		starts_at: toInstant(values.starts_at),
		ends_at: values.ends_at ? toInstant(values.ends_at) : null,
		show_on_global: values.show_on_global,
		status_page_ids: values.status_page_ids,
	});
}

const announcementService = {
	async getAnnouncements() {
		const response = await api.get('announcements').json();
		return AnnouncementListSchema.parse(response);
	},

	async getAnnouncement(announcementId: string) {
		const response = await api.get(`announcements/${announcementId}`).json();
		return AnnouncementSchema.parse(response);
	},

	async storeAnnouncement(values: AnnouncementFormValues) {
		const response = await api.post('announcements/new', { body: toBody(values) }).json();
		return CreatedAnnouncementSchema.parse(response);
	},

	async updateAnnouncement(announcementId: string, values: AnnouncementFormValues) {
		const response = await api.post(`announcements/${announcementId}/update`, { body: toBody(values) }).json();
		return CreatedAnnouncementSchema.parse(response);
	},

	async endAnnouncement(announcementId: string) {
		const response = await api.post(`announcements/${announcementId}/end`).json();
		return AnnouncementSchema.parse(response);
	},

	async deleteAnnouncement(announcementId: string) {
		await api.post(`announcements/${announcementId}/remove`);
	},
};

export default announcementService;
