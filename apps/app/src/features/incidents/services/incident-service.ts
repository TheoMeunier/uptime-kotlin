import api from '@/api/kyClient.ts';
import i18n from '@/lang/i18n.ts';
import { registerErrorMessage } from '@/api/api-error.ts';
import { getFileNameFromContentDisposition } from '@/lib/http.ts';
import {
	type Incident,
	type IncidentPage,
	IncidentPageSchema,
	IncidentSchema,
	type IncidentSummary,
	IncidentSummarySchema,
} from '@/features/incidents/schemas/incident.schema.ts';

registerErrorMessage('INCIDENT_CLOSED', () => i18n.t('incidents.acknowledge.closed'));
registerErrorMessage('INCIDENT_EXPORT_TOO_LARGE', () => i18n.t('incidents.export.too_large'));

export interface IncidentQuery {
	state?: 'open' | 'closed';
	probeId?: string;
	from?: string;
	to?: string;
	page?: number;
	size?: number;
}

function toSearchParams(query: IncidentQuery): Record<string, string | number> {
	const params: Record<string, string | number> = {};
	if (query.state) params.state = query.state;
	if (query.probeId) params.probe_id = query.probeId;
	if (query.from) params.from = query.from;
	if (query.to) params.to = query.to;
	if (query.page !== undefined) params.page = query.page;
	if (query.size !== undefined) params.size = query.size;
	return params;
}

const incidentService = {
	async getIncidents(query: IncidentQuery): Promise<IncidentPage> {
		const response = await api.get('incidents', { searchParams: toSearchParams(query) }).json();
		return IncidentPageSchema.parse(response);
	},

	async getSummary(query: Pick<IncidentQuery, 'probeId' | 'from' | 'to'>): Promise<IncidentSummary> {
		const response = await api.get('incidents/summary', { searchParams: toSearchParams(query) }).json();
		return IncidentSummarySchema.parse(response);
	},

	/** Dates are written in the browser's time zone, the one the month boundaries were computed in. */
	async exportIncidents(query: Pick<IncidentQuery, 'probeId' | 'from' | 'to'>) {
		const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
		const searchParams = { ...toSearchParams(query), ...(timeZone ? { tz: timeZone } : {}) };
		const response = await api.get('incidents/export', { searchParams });

		return {
			blob: await response.blob(),
			fileName: getFileNameFromContentDisposition(response.headers.get('Content-Disposition')) ?? 'incidents.csv',
		};
	},

	async acknowledge(incidentId: string): Promise<Incident> {
		const response = await api.post(`incidents/${incidentId}/acknowledge`).json();
		return IncidentSchema.parse(response);
	},

	async updateNote(incidentId: string, note: string): Promise<Incident> {
		const response = await api.put(`incidents/${incidentId}/note`, { body: JSON.stringify({ note }) }).json();
		return IncidentSchema.parse(response);
	},
};

export default incidentService;
