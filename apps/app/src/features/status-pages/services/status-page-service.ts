import api from '@/api/kyClient.ts';
import {
	CreatedStatusPageSchema,
	PublicStatusPageSchema,
	StatusPageDetailSchema,
	type StatusPageLayoutValue,
	StatusPageListSchema,
} from '@/features/status-pages/schemas/status-page.schema.ts';
import { StatusFeedSchema } from '@/features/status-pages/schemas/status-feed.schema.ts';
import { StatusUptimeSchema } from '@/features/status-pages/schemas/status-uptime.schema.ts';
import type { UptimePeriod } from '@/features/status-pages/lib/uptime-period.ts';

export interface StatusPagePayload {
	slug: string;
	title: string;
	description: string;
	default_layout: StatusPageLayoutValue;
	uptime_period: UptimePeriod;
	groups: { name: string; probe_ids: string[] }[];
}

function toBody(payload: StatusPagePayload) {
	return JSON.stringify({
		slug: payload.slug,
		title: payload.title,
		description: payload.description || null,
		default_layout: payload.default_layout,
		uptime_period: payload.uptime_period,
		groups: payload.groups.map((group) => ({ name: group.name || null, probe_ids: group.probe_ids })),
	});
}

const statusPageService = {
	async getStatusPages() {
		const response = await api.get('status-pages').json();
		return StatusPageListSchema.parse(response);
	},

	async getStatusPage(statusPageId: string) {
		const response = await api.get(`status-pages/${statusPageId}`).json();
		return StatusPageDetailSchema.parse(response);
	},

	async getPublicStatusPage(slug: string) {
		const response = await api.get(`status/${encodeURIComponent(slug)}`).json();
		return PublicStatusPageSchema.parse(response);
	},

	async getStatusFeed(slug?: string) {
		const path = slug ? `status/${encodeURIComponent(slug)}/feed` : 'status-feed';
		const response = await api.get(path).json();
		return StatusFeedSchema.parse(response);
	},

	async getStatusUptime(slug: string, timezone: string) {
		const response = await api
			.get(`status/${encodeURIComponent(slug)}/uptime`, { searchParams: { tz: timezone } })
			.json();
		return StatusUptimeSchema.parse(response);
	},

	async storeStatusPage(payload: StatusPagePayload) {
		const response = await api.post('status-pages/new', { body: toBody(payload) }).json();
		return CreatedStatusPageSchema.parse(response);
	},

	async updateStatusPage(statusPageId: string, payload: StatusPagePayload) {
		const response = await api.post(`status-pages/${statusPageId}/update`, { body: toBody(payload) }).json();
		return CreatedStatusPageSchema.parse(response);
	},

	async deleteStatusPage(statusPageId: string) {
		await api.post(`status-pages/${statusPageId}/remove`);
	},
};

export default statusPageService;
