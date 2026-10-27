import api from '@/api/kyClient.ts';
import { type Version, VersionSchema } from '@/features/version/schemas/version.schema.ts';

const versionService = {
	async getVersion(): Promise<Version> {
		const response = await api.get('version').json();
		return VersionSchema.parse(response);
	},
};

export default versionService;
