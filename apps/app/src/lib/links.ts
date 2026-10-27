export const DOCUMENTATION_URL = 'https://uptime-kotlin.com/docs';
export const RELEASES_URL = 'https://github.com/TheoMeunier/uptime-kotlin/releases';

export function releaseNotesUrl(version: string): string {
	return /^\d+\.\d+\.\d+/.test(version) ? `${RELEASES_URL}/tag/v${version}` : RELEASES_URL;
}
