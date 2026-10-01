import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/atoms/button.tsx';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import ErrorState from '@/components/molecules/error-state.tsx';
import LogRetentionSelect from '@/features/settings/components/log-retention-select.tsx';
import RetentionReductionDialog from '@/features/settings/components/retention-reduction-dialog.tsx';
import useLogRetentionSettings from '@/features/settings/hooks/useLogRetentionSettings.ts';
import useUpdateLogRetention from '@/features/settings/hooks/useUpdateLogRetention.ts';
import { usePreviewLogRetention } from '@/features/settings/hooks/useLogRetentionPreview.ts';
import { isValidRetentionDays, type LogRetentionPreview } from '@/features/settings/schemas/log-retention.schema.ts';

export default function LogRetentionForm() {
	const { data, isLoading, isError, refetch } = useLogRetentionSettings();

	if (isLoading) return <Skeleton className="h-56 w-full" />;
	if (isError || !data) return <ErrorState onRetry={() => refetch()} />;

	return <LogRetentionFormContent key={data.log_retention_days ?? 'forever'} current={data.log_retention_days} />;
}

function LogRetentionFormContent({ current }: { current: number | null }) {
	const { t } = useTranslation();
	const [days, setDays] = useState<number | null>(current);
	const [pendingPreview, setPendingPreview] = useState<LogRetentionPreview | null>(null);
	const { update, isLoading: isSaving } = useUpdateLogRetention();
	const { preview, isLoading: isPreviewing } = usePreviewLogRetention();

	const isValid = days === null || isValidRetentionDays(days);
	const isDirty = days !== current;
	const isPending = isSaving || isPreviewing;

	const save = () => update(days, { onSuccess: () => setPendingPreview(null) });

	const submit = () =>
		preview(days, {
			onSuccess: (result) => {
				if (result.logs_to_delete > 0) setPendingPreview(result);
				else save();
			},
		});

	return (
		<Card>
			<CardHeader>
				<CardTitle>{t('retention.title')}</CardTitle>
				<CardDescription>{t('retention.description')}</CardDescription>
			</CardHeader>

			<CardContent>
				<form
					onSubmit={(event) => {
						event.preventDefault();
						if (isValid && isDirty) submit();
					}}
				>
					<FieldGroup>
						<Field>
							<FieldLabel htmlFor="log_retention_default">{t('retention.label.default')}</FieldLabel>
							<LogRetentionSelect id="log_retention_default" value={days} onChange={setDays} disabled={isPending} />
							<FieldDescription>{t('retention.description_default')}</FieldDescription>
							{!isValid && <FieldError>{t('retention.validation')}</FieldError>}
						</Field>

						<div className="flex justify-end">
							<Button type="submit" disabled={!isValid || !isDirty || isPending}>
								{t(isPending ? 'button.loading' : 'button.save', { entity: '' })}
							</Button>
						</div>
					</FieldGroup>
				</form>
			</CardContent>

			<RetentionReductionDialog
				preview={pendingPreview}
				isPending={isSaving}
				onCancel={() => setPendingPreview(null)}
				onConfirm={save}
			/>
		</Card>
	);
}
