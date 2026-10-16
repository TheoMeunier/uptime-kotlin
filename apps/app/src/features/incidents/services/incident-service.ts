import api from '@/api/kyClient.ts';
import i18n from '@/lang/i18n.ts';
import { registerErrorMessage } from '@/api/api-error.ts';
import {
	type Incident,
	type IncidentPage,
	IncidentPageSchema,
	IncidentSchema,
	type IncidentSummary,
	IncidentSummarySchema,
} from '@/features/incidents/schemas/incident.schema.ts';

registerErrorMessage('INCIDENT_CLOSED', () => i18n.t('incidents.acknowledge.closed'));

export interface IncidentQuery {
	state?: 'open' | 'closed';
	probeId?: string;
	from?: string;
	page?: number;
	size?: number;
}

function toSearchParams(query: IncidentQuery): Record<string, string | number> {
	const params: Record<string, string | number> = {};
	if (query.state) params.state = query.state;
	if (query.probeId) params.probe_id = query.probeId;
	if (query.from) params.from = query.from;
	if (query.page !== undefined) params.page = query.page;
	if (query.size !== undefined) params.size = query.size;
	return params;
}

const incidentService = {
	async getIncidents(query: IncidentQuery): Promise<IncidentPage> {
		const response = await api.get('incidents', { searchParams: toSearchParams(query) }).json();
		return IncidentPageSchema.parse(response);
	},

	async getSummary(query: Pick<IncidentQuery, 'probeId' | 'from'>): Promise<IncidentSummary> {
		const response = await api.get('incidents/summary', { searchParams: toSearchParams(query) }).json();
		return IncidentSummarySchema.parse(response);
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
