import { type StoreProbeSchema, useProbeForm } from '@/features/probes/hooks/useProbeForm.ts';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field.tsx';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import { useTranslation } from 'react-i18next';
import FormSelect from '@/components/molecules/forms/form-select.tsx';
import ProbeProtocol, { PROTOCOL_GROUPS, PROTOCOL_LABELS } from '@/features/probes/enums/probe-enum.ts';
import { Input } from '@/components/atoms/input.tsx';
import FormFieldNotification from '@/features/notifications/components/forms/form-field-notification.tsx';
import FormSwitch from '@/components/molecules/forms/form-switch.tsx';
import { Textarea } from '@/components/atoms/textarea.tsx';
import { Activity, Bell, Clock, Settings2 } from 'lucide-react';
import FormSelectNotification from '@/features/notifications/components/forms/form-select-notification.tsx';
import CreateNotificationDialogue from '@/features/notifications/components/actions/create-notification-dialogue.tsx';
import { Button } from '@/components/atoms/button.tsx';
import { buildProbeFieldsConfig } from '@/features/probes/components/config/probe-type.ts';
import { Link } from 'react-router';
import HttpAdvancedFieldsForm from '@/features/probes/components/forms/http-advanced-fields-form.tsx';
import UnsavedChangesGuard from '@/components/molecules/forms/unsaved-changes-guard.tsx';
import { type ComponentType, type ReactNode, useMemo, useState } from 'react';
import { Controller, type FieldPath, type FieldPathValue } from 'react-hook-form';
import LogRetentionSelect from '@/features/settings/components/log-retention-select.tsx';
import RetentionReductionDialog from '@/features/settings/components/retention-reduction-dialog.tsx';
import useLogRetentionSettings from '@/features/settings/hooks/useLogRetentionSettings.ts';
import type { LogRetentionPreview } from '@/features/settings/schemas/log-retention.schema.ts';
import { usePreviewProbeLogRetention } from '@/features/settings/hooks/useLogRetentionPreview.ts';

type ProbeFormMode = 'create' | 'edit';

interface ProbeFormProps {
	mode: ProbeFormMode;
	defaultValues: Partial<StoreProbeSchema>;
	cancelLink: string;
	onSubmit: (values: StoreProbeSchema) => void;
	isLoading?: boolean;
	probeId?: string;
}

function FormSection({
	title,
	description,
	icon: Icon,
	children,
}: {
	title: string;
	description?: string;
	icon: ComponentType<{ className?: string }>;
	children: ReactNode;
}) {
	return (
		<Card>
			<CardHeader>
				<CardTitle className="flex items-center gap-2 text-base">
					<Icon className="text-muted-foreground size-4" />
					{title}
				</CardTitle>
				{description && <CardDescription>{description}</CardDescription>}
			</CardHeader>
			<CardContent>
				<FieldGroup>{children}</FieldGroup>
			</CardContent>
		</Card>
	);
}

export default function ProbeForm({ mode, defaultValues, cancelLink, isLoading, onSubmit, probeId }: ProbeFormProps) {
	const { t } = useTranslation();
	const { form, errors } = useProbeForm({ defaultValues });
	const protocol = form.watch('protocol');
	const { data: retentionSettings } = useLogRetentionSettings();
	const { preview: previewRetention } = usePreviewProbeLogRetention(probeId);
	const [retentionConfirmation, setRetentionConfirmation] = useState<{
		preview: LogRetentionPreview;
		values: StoreProbeSchema;
	} | null>(null);

	const submit = async (values: StoreProbeSchema) => {
		const retention = values.log_retention_days ?? null;
		const initialRetention = defaultValues.log_retention_days ?? null;

		if (mode === 'edit' && probeId && retention !== initialRetention) {
			try {
				const preview = await previewRetention(retention);
				if (preview.logs_to_delete > 0) {
					setRetentionConfirmation({ preview, values });
					return;
				}
			} catch {
				// Preview unavailable: the backend still validates the value, and
				// nothing is deleted before the nightly purge.
			}
		}

		onSubmit(values);
	};

	const PROBE_FIELDS_CONFIG = useMemo(() => buildProbeFieldsConfig(t), [t]);
	const dynamicFields = protocol ? PROBE_FIELDS_CONFIG[protocol] : PROBE_FIELDS_CONFIG[ProbeProtocol.HTTP];
	const hasAdvancedFields = Boolean(dynamicFields?.advanced_fields?.length);

	const handleProtocolChange = (value: string) => {
		const nextProtocol = value as ProbeProtocol;
		const config = PROBE_FIELDS_CONFIG[nextProtocol];
		const fields = [...config.fields, ...config.advanced_fields];

		fields.forEach((field) => {
			const fieldName = field.name as FieldPath<StoreProbeSchema>;
			const defaultValue = 'default_value' in field ? field.default_value : undefined;

			form.setValue(fieldName, defaultValue as FieldPathValue<StoreProbeSchema, typeof fieldName>, {
				shouldDirty: true,
				shouldValidate: false,
			});
		});
	};

	return (
		<form onSubmit={form.handleSubmit(submit)}>
			<UnsavedChangesGuard when={form.formState.isDirty && !isLoading} />

			<div className="grid gap-6 lg:grid-cols-3">
				<div className="flex flex-col gap-6 lg:col-span-2">
					<FormSection title={t('monitors.section.target')} icon={Activity}>
						<Field>
							<FieldLabel htmlFor="protocol">{t('monitors.label.protocol')}</FieldLabel>
							<FormSelect
								form={form}
								name="protocol"
								options={PROTOCOL_GROUPS.map((group) => ({
									label: t(group.labelKey),
									options: group.protocols.map((protocol) => ({
										value: protocol,
										label: PROTOCOL_LABELS[protocol],
									})),
								}))}
								onValueChange={handleProtocolChange}
							/>
							<FieldError>{errors.protocol?.message}</FieldError>
						</Field>

						<Field>
							<FieldLabel htmlFor="name">{t('monitors.label.name_monitor')}</FieldLabel>
							<Input
								{...form.register('name')}
								id="name"
								placeholder={t('monitors.placeholder.name_monitor')}
								required
							/>
							<FieldError>{errors.name?.message}</FieldError>
						</Field>

						{protocol &&
							dynamicFields?.fields.map((field) => (
								<FormFieldNotification key={field.name} field={field} form={form} />
							))}
					</FormSection>

					<FormSection
						title={t('monitors.section.schedule')}
						description={t('monitors.section.schedule_description')}
						icon={Clock}
					>
						<div className="grid gap-4 sm:grid-cols-3">
							<Field>
								<FieldLabel htmlFor="interval">{t('monitors.label.interval')}</FieldLabel>
								<Input
									{...form.register('interval', { valueAsNumber: true })}
									id="interval"
									type="number"
									min={10}
									required
								/>
								<FieldError>{errors.interval?.message}</FieldError>
							</Field>

							<Field>
								<FieldLabel htmlFor="retry">{t('monitors.label.retry')}</FieldLabel>
								<Input {...form.register('retry', { valueAsNumber: true })} id="retry" type="number" min={0} required />
								<FieldError>{errors.retry?.message}</FieldError>
							</Field>

							<Field>
								<FieldLabel htmlFor="interval_retry">{t('monitors.label.interval_retry')}</FieldLabel>
								<Input
									{...form.register('interval_retry', { valueAsNumber: true })}
									id="interval_retry"
									type="number"
									min={0}
									required
								/>
								<FieldError>{errors.interval_retry?.message}</FieldError>
							</Field>
						</div>

						<FieldDescription>{t('monitors.description.internal_retry')}</FieldDescription>
					</FormSection>

					{protocol === ProbeProtocol.HTTP && (
						<Card>
							<CardContent>
								<HttpAdvancedFieldsForm form={form} />
							</CardContent>
						</Card>
					)}
				</div>

				<div className="flex flex-col gap-6">
					<FormSection
						title={t('notifications.title.notifications')}
						description={t('monitors.section.notifications_description')}
						icon={Bell}
					>
						<FormSelectNotification form={form} name="notifications" />
						<CreateNotificationDialogue />

						<Field>
							<FieldLabel htmlFor="alert_repeat_seconds">{t('monitors.label.alert_repeat_seconds')}</FieldLabel>
							<Input
								{...form.register('alert_repeat_seconds', { valueAsNumber: true })}
								id="alert_repeat_seconds"
								type="number"
								min={0}
								step={60}
							/>
							<FieldDescription>{t('monitors.description.alert_repeat_seconds')}</FieldDescription>
							<FieldError>{errors.alert_repeat_seconds?.message}</FieldError>
						</Field>
					</FormSection>

					<FormSection title={t('monitors.section.settings')} icon={Settings2}>
						<Field>
							<FormSwitch form={form} name="enabled" label={t('form.label.enabled')} />
							<FieldDescription>{t('monitors.description.enabled')}</FieldDescription>
							<FieldError>{errors.enabled?.message}</FieldError>
						</Field>

						{protocol &&
							hasAdvancedFields &&
							dynamicFields.advanced_fields.map((field) => (
								<FormFieldNotification key={field.name} field={field} form={form} />
							))}

						<Field>
							<FieldLabel htmlFor="log_retention_days">{t('retention.label.probe')}</FieldLabel>
							<Controller
								control={form.control}
								name="log_retention_days"
								render={({ field }) => (
									<LogRetentionSelect
										id="log_retention_days"
										value={field.value}
										onChange={field.onChange}
										allowInherit
										inheritedDays={retentionSettings?.log_retention_days ?? null}
									/>
								)}
							/>
							<FieldDescription>{t('retention.description_probe')}</FieldDescription>
							<FieldError>{errors.log_retention_days?.message}</FieldError>
						</Field>

						<Field>
							<FieldLabel htmlFor="description">{t('form.label.description')}</FieldLabel>
							<Textarea {...form.register('description')} id="description" rows={4} />
							<FieldError>{errors.description?.message}</FieldError>
						</Field>
					</FormSection>
				</div>
			</div>

			<div className="bg-background/95 border-border sticky bottom-0 mt-6 flex items-center justify-end gap-3 border-t py-4 backdrop-blur">
				<Button variant="outline" asChild>
					<Link to={cancelLink}>{t('button.cancel')}</Link>
				</Button>
				<Button type="submit" disabled={isLoading}>
					{t(isLoading ? 'button.loading' : mode === 'create' ? 'button.create' : 'button.update', {
						entity: t('entity.monitor'),
					})}
				</Button>
			</div>

			<RetentionReductionDialog
				preview={retentionConfirmation?.preview ?? null}
				isPending={isLoading}
				onCancel={() => setRetentionConfirmation(null)}
				onConfirm={() => {
					const values = retentionConfirmation?.values;
					setRetentionConfirmation(null);
					if (values) onSubmit(values);
				}}
			/>
		</form>
	);
}
