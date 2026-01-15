import type { StoreProbeSchema } from '@/features/probes/hooks/useStoreProbeForm.ts';
import api from '@/api/kyClient.ts';
import probeResponseSchema, { type ProbeListItem } from '@/features/probes/schemas/probe-response.schema.ts';
import { z } from 'zod';

const probeService = {
	async getProbes(): Promise<ProbeListItem[]> {
		const response = await api.get('probes').json();
		return probeResponseSchema.parse(response);
	},

	async getProbesStatus() {
		return await api.get('probes/status').json();
	},

	async getProbe<T>(id: string, hours: number, schema: z.ZodSchema<T>): Promise<T> {
		const response = await api.get(`probes/${id}?hours=${hours}`).json();
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
};

export default probeService;
