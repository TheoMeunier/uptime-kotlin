import { useTranslation } from 'react-i18next';
import { FileSearch, Upload } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import {
	Field,
	FieldDescription,
	FieldError,
	FieldGroup,
	FieldLabel,
	FieldSet,
	FieldLegend,
} from '@/components/atoms/field.tsx';
import FileDropzone from '@/components/molecules/forms/file-dropzone.tsx';
import ConflictStrategyChoice from '@/features/backup/components/conflict-strategy-choice.tsx';
import RestoreReportTable from '@/features/backup/components/restore-report-table.tsx';
import useRestoreFlow from '@/features/backup/hooks/useRestoreFlow.ts';

export default function RestoreCard() {
	const { t } = useTranslation();
	const flow = useRestoreFlow();

	return (
		<Card>
			<CardHeader>
				<CardTitle>{t('backup.restore.title')}</CardTitle>
				<CardDescription>{t('backup.restore.description')}</CardDescription>
			</CardHeader>
			<CardContent>
				<FieldGroup>
					<Field>
						<FieldLabel htmlFor="restore-file">{t('backup.restore.file')}</FieldLabel>
						<FileDropzone
							id="restore-file"
							file={flow.source}
							onChange={flow.selectFile}
							accept="application/json,.json"
							hint={t('backup.restore.file_hint')}
							disabled={flow.isRestoring}
							invalid={flow.fileError}
						/>
						{flow.fileError && <FieldError>{t('backup.restore.invalid_json')}</FieldError>}
					</Field>

					<FieldSet>
						<FieldLegend variant="label">{t('backup.restore.conflict')}</FieldLegend>
						<FieldDescription>{t('backup.restore.conflict_description')}</FieldDescription>
						<ConflictStrategyChoice value={flow.onConflict} onChange={flow.setOnConflict} disabled={flow.isRestoring} />
					</FieldSet>

					{flow.report && flow.counts && (
						<div className="flex flex-col gap-3">
							<p className="text-muted-foreground text-sm">
								{t(`backup.restore.summary.${flow.report.dry_run ? 'preview' : 'done'}`, flow.counts)}
							</p>
							<RestoreReportTable report={flow.report} />
						</div>
					)}
				</FieldGroup>
			</CardContent>
			<CardFooter className="justify-end gap-2">
				<Button type="button" variant="outline" disabled={!flow.canAnalyze} onClick={flow.analyze}>
					<FileSearch className="size-4" />
					{t('backup.restore.analyze')}
				</Button>
				{flow.canConfirm && flow.counts && (
					<Button type="button" disabled={flow.isRestoring} onClick={flow.confirm}>
						<Upload className="size-4" />
						{t(flow.isRestoring ? 'backup.restore.restoring' : 'backup.restore.confirm', {
							count: flow.counts.create,
						})}
					</Button>
				)}
			</CardFooter>
		</Card>
	);
}
