import api from '@/api/kyClient.ts';
import {
	CreatedMaintenanceSchema,
	MaintenanceDetailSchema,
	MaintenanceListSchema,
	type MaintenanceDetail,
	type MaintenanceListItem,
} from '@/features/maintenances/schemas/maintenance.schema.ts';
import type { StoreMaintenanceSchema } from '@/features/maintenances/hooks/useMaintenanceForm.ts';
import { zonedWallClockToInstant } from '@/lib/datetime.ts';

function toPayload(data: StoreMaintenanceSchema) {
	return {
		title: data.title,
		description: data.description || null,
		starts_at: zonedWallClockToInstant(data.starts_at, data.timezone),
		duration_seconds: data.duration_minutes * 60,
		recurrence: data.recurrence,
		recurrence_until:
			data.recurrence !== 'ONCE' && data.recurrence_until
				? zonedWallClockToInstant(data.recurrence_until, data.timezone)
				: null,
		timezone: data.timezone,
		active: data.active,
		is_public: data.is_public,
		probe_ids: data.probe_ids,
	};
}

const maintenanceService = {
	async getMaintenances(): Promise<MaintenanceListItem[]> {
		const response = await api.get('maintenances').json();
		return MaintenanceListSchema.parse(response);
	},

	async getMaintenance(maintenanceId: string): Promise<MaintenanceDetail> {
		const response = await api.get(`maintenances/${maintenanceId}`).json();
		return MaintenanceDetailSchema.parse(response);
	},

	async getProbeMaintenances(probeId: string): Promise<MaintenanceListItem[]> {
		const response = await api.get(`probes/${probeId}/maintenances`).json();
		return MaintenanceListSchema.parse(response);
	},

	async storeMaintenance(data: StoreMaintenanceSchema) {
		const response = await api.post('maintenances/new', { body: JSON.stringify(toPayload(data)) }).json();
		return CreatedMaintenanceSchema.parse(response);
	},

	async updateMaintenance(maintenanceId: string, data: StoreMaintenanceSchema) {
		const response = await api
			.post(`maintenances/${maintenanceId}/update`, { body: JSON.stringify(toPayload(data)) })
			.json();
		return CreatedMaintenanceSchema.parse(response);
	},

	async deleteMaintenance(maintenanceId: string) {
		await api.post(`maintenances/${maintenanceId}/remove`);
	},

	async endMaintenance(maintenanceId: string) {
		await api.post(`maintenances/${maintenanceId}/end`).json();
	},

	async cancelOccurrence(occurrenceId: string) {
		await api.post(`maintenances/occurrences/${occurrenceId}/cancel`);
	},

	async startAdHoc(probeId: string, durationMinutes: number, title?: string) {
		const response = await api
			.post(`probes/${probeId}/maintenance`, {
				body: JSON.stringify({ duration_minutes: durationMinutes, title: title ?? null }),
			})
			.json();

		return CreatedMaintenanceSchema.parse(response);
	},
};

export default maintenanceService;
