import api from '@/api/kyClient.ts';
import probeResponseSchema, {
	type ProbeListItem,
	ProbeStatusShowSchema,
} from '@/features/probes/schemas/probe-response.schema.ts';

import { z } from 'zod';
import type { StoreProbeSchema } from '@/features/probes/hooks/useProbeForm.ts';

function getFileNameFromContentDisposition(contentDisposition: string | null) {
	const match = contentDisposition?.match(/filename="?(?<filename>[^"]+)"?/);
	return match?.groups?.filename;
}

const probeService = {
	async getProbes(): Promise<ProbeListItem[]> {
		const response = await api.get('probes').json();
		return probeResponseSchema.parse(response);
	},

	async getProbesStatus() {
		const response = await api.get('probes/status').json();

		return ProbeStatusShowSchema.parse(response);
	},

	async getProbe<T>(id: string, hours: number, schema: z.ZodSchema<T>): Promise<T> {
		const response = await api.get(`probes/${id}?hours=${hours}`).json();
		return schema.parse(response);
	},

	async getProbeForUpdate<T>(id: string, schema: z.ZodSchema<T>): Promise<T> {
		const response = await api.get(`probes/${id}/edit`).json();
		return schema.parse(response);
	},

	async updateProbe(id: string, data: StoreProbeSchema) {
		await api
			.post(`probes/${id}/update`, {
				body: JSON.stringify(data),
			})
			.json();
	},

	async storeProbe(data: StoreProbeSchema) {
		await api
			.post('probes/new', {
				body: JSON.stringify(data),
			})
			.json();
	},

	async onoffline(id: string, enabled: boolean) {
		await api
			.post(`probes/${id}/update-on-off`, {
				body: JSON.stringify({ enabled }),
			})
			.json();
	},

	async deleteProbe(id: string) {
		await api.post(`probes/${id}/remove`).json();
	},

	async purgeProbeLogs(id: string) {
		await api.post(`probes/${id}/logs/purge`).json();
	},

	async exportProbeLogs(id: string) {
		const response = await api.get(`probes/${id}/logs/export`);
		const blob = await response.blob();
		const fileName = getFileNameFromContentDisposition(response.headers.get('Content-Disposition'));

		return {
			blob,
			fileName: fileName ?? `monitor-logs-${id}.csv`,
		};
	},
};

export default probeService;
