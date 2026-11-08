import api from '@/api/kyClient.ts';
import {
	type DefaultStatusPageSettings,
	DefaultStatusPageSettingsSchema,
} from '@/features/settings/schemas/default-status-page.schema.ts';
import {
	type LogRetentionPreview,
	LogRetentionPreviewSchema,
	type LogRetentionSettings,
	LogRetentionSettingsSchema,
} from '@/features/settings/schemas/log-retention.schema.ts';

function daysParam(days: number | null | undefined): Record<string, string> {
	return days === null || days === undefined ? {} : { days: String(days) };
}

const settingsService = {
	async getLogRetention(): Promise<LogRetentionSettings> {
		const response = await api.get('settings/retention').json();
		return LogRetentionSettingsSchema.parse(response);
	},

	async updateLogRetention(days: number | null): Promise<LogRetentionSettings> {
		const response = await api.put('settings/retention', { json: { log_retention_days: days } }).json();
		return LogRetentionSettingsSchema.parse(response);
	},

	async getDefaultStatusPage(): Promise<DefaultStatusPageSettings> {
		const response = await api.get('settings/status-page').json();
		return DefaultStatusPageSettingsSchema.parse(response);
	},

	async updateDefaultStatusPage(isPrivate: boolean): Promise<DefaultStatusPageSettings> {
		const response = await api.put('settings/status-page', { json: { is_private: isPrivate } }).json();
		return DefaultStatusPageSettingsSchema.parse(response);
	},

	async previewLogRetention(days: number | null): Promise<LogRetentionPreview> {
		const response = await api.get('settings/retention/preview', { searchParams: daysParam(days) }).json();
		return LogRetentionPreviewSchema.parse(response);
	},

	async previewProbeLogRetention(probeId: string, days: number | null): Promise<LogRetentionPreview> {
		const response = await api
			.get(`probes/${probeId}/logs/retention-preview`, { searchParams: daysParam(days) })
			.json();
		return LogRetentionPreviewSchema.parse(response);
	},
};

export default settingsService;
