import { z } from 'zod';

export const BACKUP_ITEM_KINDS = ['NOTIFICATION', 'PROBE', 'MAINTENANCE', 'STATUS_PAGE', 'ANNOUNCEMENT'] as const;
export const RESTORE_OUTCOMES = ['CREATE', 'RENAME', 'SKIP', 'ERROR'] as const;
export const RESTORE_ISSUE_CODES = [
	'MISSING_KEY',
	'SECRET_UNREADABLE',
	'INVALID',
	'ALREADY_EXISTS',
	'DUPLICATE_IN_FILE',
	'UNKNOWN_REFERENCE',
	'PUSH_URL_REGENERATED',
] as const;
export const SECRETS_MODES = ['encrypted', 'plain'] as const;
export const CONFLICT_STRATEGIES = ['skip', 'rename'] as const;

export type BackupItemKind = (typeof BACKUP_ITEM_KINDS)[number];
export type RestoreOutcome = (typeof RESTORE_OUTCOMES)[number];
export type SecretsMode = (typeof SECRETS_MODES)[number];
export type ConflictStrategy = (typeof CONFLICT_STRATEGIES)[number];

export const RestoreIssueSchema = z.object({
	code: z.enum(RESTORE_ISSUE_CODES),
	detail: z.string().nullable(),
	target: z.enum(BACKUP_ITEM_KINDS).nullable(),
});

export type RestoreIssue = z.infer<typeof RestoreIssueSchema>;

export const RestoreItemReportSchema = z.object({
	kind: z.enum(BACKUP_ITEM_KINDS),
	ref: z.string().nullable(),
	name: z.string(),
	outcome: z.enum(RESTORE_OUTCOMES),
	new_name: z.string().nullable(),
	issues: z.array(RestoreIssueSchema),
});

export type RestoreItemReport = z.infer<typeof RestoreItemReportSchema>;

export const RestoreReportSchema = z.object({
	dry_run: z.boolean(),
	items: z.array(RestoreItemReportSchema),
});

export type RestoreReport = z.infer<typeof RestoreReportSchema>;

export type RestoreCounts = { create: number; skip: number; error: number };

export function countOutcomes(report: RestoreReport): RestoreCounts {
	const count = (...outcomes: RestoreOutcome[]) =>
		report.items.filter((item) => outcomes.includes(item.outcome)).length;

	return { create: count('CREATE', 'RENAME'), skip: count('SKIP'), error: count('ERROR') };
}
