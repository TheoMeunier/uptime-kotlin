import api from '@/api/kyClient.ts';
import dashboardStatsSchema from '@/features/dashboard/schemas/dashboard-stats.schema.ts';

const dashboardService = {
	async getStats() {
		const response = await api.get('dashboard/stats').json();
		return dashboardStatsSchema.parse(response);
	},
};

export default dashboardService;
