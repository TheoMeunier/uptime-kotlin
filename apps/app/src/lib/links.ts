export const DOCUMENTATION_URL = 'https://uptime-kotlin.com/docs';
export const EMAIL_NOTIFICATION_DOCS_URL = `${DOCUMENTATION_URL}/email-notifications`;
export const WEBSITE_URL = 'https://uptime-kotlin.com';
export const REPOSITORY_URL = 'https://github.com/TheoMeunier/uptime-kotlin';
export const RELEASES_URL = `${REPOSITORY_URL}/releases`;
export const ISSUES_URL = `${REPOSITORY_URL}/issues`;

const MAX_PREFILLED_LOGS = 2000;

interface NewIssueOptions {
	version?: string;
	title?: string;
	logs?: string;
}

export function newIssueUrl({ version, title, logs }: NewIssueOptions = {}): string {
	const params = new URLSearchParams({ template: 'bug_report.yml' });
	if (version) params.set('version', version);
	if (title) params.set('title', `[Bug] ${title}`);
	if (logs) params.set('logs', logs.length > MAX_PREFILLED_LOGS ? `${logs.slice(0, MAX_PREFILLED_LOGS)}\n…` : logs);
	return `${ISSUES_URL}/new?${params.toString()}`;
}

export function releaseNotesUrl(version: string): string {
	return /^\d+\.\d+\.\d+/.test(version) ? `${RELEASES_URL}/tag/v${version}` : RELEASES_URL;
}
