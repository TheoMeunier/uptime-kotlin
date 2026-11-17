import { useTranslation } from 'react-i18next';
import { Badge } from '@/components/atoms/badge.tsx';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/atoms/table.tsx';
import RestoreIssueLine from '@/features/backup/components/restore-issue-line.tsx';
import type { RestoreReport, RestoreItemReport, RestoreOutcome } from '@/features/backup/schemas/backup.schema.ts';

const OUTCOME_VARIANT: Record<RestoreOutcome, 'default' | 'secondary' | 'outline' | 'destructive'> = {
	CREATE: 'default',
	RENAME: 'secondary',
	SKIP: 'outline',
	ERROR: 'destructive',
};

export default function RestoreReportTable({ report }: { report: RestoreReport }) {
	const { t } = useTranslation();

	if (report.items.length === 0) {
		return <p className="text-muted-foreground text-sm">{t('backup.restore.nothing')}</p>;
	}

	return (
		<div className="max-h-96 overflow-auto rounded-md border">
			<Table>
				<TableHeader>
					<TableRow>
						<TableHead>{t('backup.column.kind')}</TableHead>
						<TableHead>{t('backup.column.name')}</TableHead>
						<TableHead>{t('backup.column.result')}</TableHead>
					</TableRow>
				</TableHeader>
				<TableBody>
					{report.items.map((item, index) => (
						<RestoreReportRow key={`${item.kind}-${index}`} item={item} dryRun={report.dry_run} />
					))}
				</TableBody>
			</Table>
		</div>
	);
}

function RestoreReportRow({ item, dryRun }: { item: RestoreItemReport; dryRun: boolean }) {
	const { t } = useTranslation();

	return (
		<TableRow className="align-top">
			<TableCell className="text-muted-foreground whitespace-nowrap">{t(`backup.kind.${item.kind}`)}</TableCell>
			<TableCell className="whitespace-normal">
				<div className="font-medium break-words">{item.new_name ?? item.name}</div>
				{item.new_name && (
					<div className="text-muted-foreground text-xs">{t('backup.restore.renamed_from', { name: item.name })}</div>
				)}
				{item.issues.map((issue, index) => (
					<RestoreIssueLine key={`${issue.code}-${index}`} issue={issue} />
				))}
			</TableCell>
			<TableCell>
				<Badge variant={OUTCOME_VARIANT[item.outcome]}>
					{t(`backup.outcome.${dryRun ? 'preview' : 'done'}.${item.outcome}`)}
				</Badge>
			</TableCell>
		</TableRow>
	);
}
