import api from '@/api/kyClient.ts';
import { type Version, VersionSchema } from '@/features/version/schemas/version.schema.ts';

let lastKnownVersion: string | undefined;

const versionService = {
	async getVersion(): Promise<Version> {
		const response = await api.get('version').json();
		const version = VersionSchema.parse(response);
		lastKnownVersion = version.version;
		return version;
	},

	lastKnownVersion(): string | undefined {
		return lastKnownVersion;
	},
};

export default versionService;
