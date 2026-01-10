import {
	Field,
	FieldDescription,
	FieldError,
	FieldGroup,
	FieldLabel,
	FieldLegend,
	FieldSet,
} from '@/components/atoms/field.tsx';
import { Input } from '@/components/atoms/input.tsx';
import { Button } from '@/components/atoms/button.tsx';
import ProbeProtocol from '@/features/probes/enums/probe-enum.ts';
import { Bell } from 'lucide-react';
import { useStoreProbeForm } from '@/features/probes/hooks/useStoreProbeForm.ts';
import { Textarea } from '@/components/atoms/textarea.tsx';
import FormSwitch from '@/components/molecules/forms/form-switch.tsx';
import FormSelect from '@/components/molecules/forms/form-select.tsx';
import CreateNotificationDialogue from '@/features/notifications/components/actions/create-notification-dialogue.tsx';
import FormSelectNotification from '@/features/notifications/components/forms/form-select-notification.tsx';
import PROBE_FIELDS_CONFIG from '@/features/probes/components/config/probe-type.ts';
import FormFieldNotification from '@/features/notifications/components/forms/form-field-notification.tsx';
import { useTranslation } from 'react-i18next';

export default function CreateProbeForm() {
	const { t } = useTranslation();
	const { form, isLoading, onsubmit, errors } = useStoreProbeForm();
	const protocol = form.watch('protocol');

	const dynamicFields = protocol ? PROBE_FIELDS_CONFIG[protocol] : PROBE_FIELDS_CONFIG[ProbeProtocol.HTTP];

	console.log(dynamicFields);

	return (
		<div>
			<form onSubmit={form.handleSubmit(onsubmit)}>
				<div className="grid grid-cols-2 gap-8">
					<FieldGroup>
						<FieldSet>
							<FieldLegend>{t('monitors.title.create')}</FieldLegend>
							<FieldDescription>All transactions are secure and encrypted</FieldDescription>
							<FieldGroup>
								<Field className="space-y-2">
									<FieldLabel htmlFor="protocol">{t('monitors.label.protocol')}</FieldLabel>
									<FormSelect form={form} name="protocol" options={Object.values(ProbeProtocol)} />
									<FieldError>{errors.protocol?.message}</FieldError>
								</Field>
								<Field>
									<FieldLabel htmlFor="name">{t('monitors.label.name_monitor')}</FieldLabel>
									<Input {...form.register('name')} id="name" placeholder="Evil Rabbit" required />
									<FieldError>{errors.name?.message}</FieldError>
								</Field>

								{protocol && dynamicFields && (
									<FieldGroup>
										{dynamicFields.fields.map((field) => (
											<FormFieldNotification key={field.name} field={field} form={form} />
										))}
									</FieldGroup>
								)}

								<Field>
									<FieldLabel htmlFor="retry">{t('monitors.label.retry')}</FieldLabel>
									<Input
										{...form.register('retry', { valueAsNumber: true })}
										id="retry"
										type="number"
										min={0}
										required
									/>
									<FieldError>{errors.retry?.message}</FieldError>
								</Field>
								<Field>
									<FieldLabel htmlFor="interval">Interval value (in seconds) default 60</FieldLabel>
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
									<FieldLabel htmlFor="interval_retry">{t('monitors.label.internal_retry')}</FieldLabel>
									<Input
										{...form.register('interval_retry', {
											valueAsNumber: true,
										})}
										id="interval_retry"
										type="number"
										min={0}
										required
									/>
									<FieldError>{errors.interval_retry?.message}</FieldError>
									<FieldDescription>{t('monitors.description.internal_retry')}</FieldDescription>
								</Field>
							</FieldGroup>
						</FieldSet>

						<FieldGroup>
							<FieldLegend>Advanced</FieldLegend>
							<Field className="mb-2">
								<div className="flex items-center space-x-2">
									<FormSwitch form={form} name={'enabled'} label={t('form.label.enabled')} />
								</div>
								<FieldError>{errors.enabled?.message}</FieldError>
							</Field>

							{protocol && dynamicFields && (
								<FieldGroup>
									{dynamicFields.advanced_fields.map((field) => (
										<FormFieldNotification key={field.name} field={field} form={form} />
									))}
								</FieldGroup>
							)}

							<Field className="space-y-2">
								<FieldLabel htmlFor="description">{t('form.label.description')}</FieldLabel>
								<Textarea {...form.register('description')} id="description" rows={5} />
								<FieldError>{errors.description?.message}</FieldError>
							</Field>
						</FieldGroup>
					</FieldGroup>

					<FieldGroup>
						<FieldSet>
							<FieldLegend className="flex items-center gap-2">
								<Bell /> {t('notifications.title.notifications')}
							</FieldLegend>
							<FieldGroup className="mt-4">
								<FormSelectNotification form={form} />
								<CreateNotificationDialogue />
							</FieldGroup>
						</FieldSet>
					</FieldGroup>
				</div>

				<FieldGroup className="mt-6">
					<Field orientation="horizontal">
						<Button variant="outline" type="button">
							{t('button.cancel')}
						</Button>
						<Button type="submit">
							{t(isLoading ? 'button.loading' : 'button.create', {
								entity: t('entity.monitor'),
							})}
						</Button>
					</Field>
				</FieldGroup>
			</form>
		</div>
	);
}
