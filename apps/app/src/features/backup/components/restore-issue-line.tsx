import { useTranslation } from 'react-i18next';
import { cn } from '@/lib/utils';
import type { RestoreIssue } from '@/features/backup/schemas/backup.schema.ts';

const BLOCKING_CODES: ReadonlySet<RestoreIssue['code']> = new Set(['MISSING_KEY', 'SECRET_UNREADABLE', 'INVALID']);

export default function RestoreIssueLine({ issue }: { issue: RestoreIssue }) {
	const { t } = useTranslation();
	const target = issue.target ? t(`backup.kind.${issue.target}`) : '';

	return (
		<div className={cn('text-xs', BLOCKING_CODES.has(issue.code) ? 'text-destructive' : 'text-muted-foreground')}>
			{t(`backup.issue.${issue.code}`, { detail: issue.detail ?? '', target })}
		</div>
	);
}
