import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { DialogFooter, DialogHeader, DialogTitle } from '@/components/atoms/dialog.tsx';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field.tsx';
import { Input } from '@/components/atoms/input.tsx';
import { Textarea } from '@/components/atoms/textarea.tsx';
import { Button } from '@/components/atoms/button.tsx';
import FormSelect from '@/components/molecules/forms/form-select.tsx';
import FormSwitch from '@/components/molecules/forms/form-switch.tsx';
import FormMultiSelect from '@/components/molecules/forms/form-select-multiple.tsx';
import probeService from '@/features/probes/services/probeService.ts';
import useMaintenanceForm, { type StoreMaintenanceSchema } from '@/features/maintenances/hooks/useMaintenanceForm.ts';
import { MaintenanceRecurrenceEnum } from '@/features/maintenances/schemas/maintenance.schema.ts';
import { timezoneOptions } from '@/lib/datetime.ts';

interface MaintenanceFormProps {
	mode: 'create' | 'update';
	defaultValues?: Partial<StoreMaintenanceSchema>;
	onSubmit: (values: StoreMaintenanceSchema) => void;
	isLoading?: boolean;
}

export default function MaintenanceForm({ mode, defaultValues = {}, isLoading, onSubmit }: MaintenanceFormProps) {
	const { t } = useTranslation();
	const { form, errors } = useMaintenanceForm(defaultValues);

	const { data: probes } = useQuery({
		queryKey: ['probes'],
		queryFn: async () => probeService.getProbes(),
	});

	const recurrence = form.watch('recurrence');

	const handleSubmit = (event: React.FormEvent) => {
		event.preventDefault();
		event.stopPropagation();
		form.handleSubmit(onSubmit)(event);
	};

	return (
		<form onSubmit={handleSubmit}>
			<DialogHeader>
				<DialogTitle>
					{t(mode === 'create' ? 'button.create' : 'button.update', { entity: t('entity.maintenance') })}
				</DialogTitle>
			</DialogHeader>

			<FieldGroup className="mt-4">
				<Field>
					<FieldLabel htmlFor="title">{t('form.label.name', { entity: '' }).trim()}</FieldLabel>
					<Input id="title" {...form.register('title')} placeholder={t('maintenances.form.title_placeholder')} />
					<FieldError>{errors.title?.message}</FieldError>
				</Field>

				<Field>
					<FieldLabel htmlFor="description">{t('form.label.description')}</FieldLabel>
					<Textarea id="description" {...form.register('description')} />
					<FieldError>{errors.description?.message}</FieldError>
				</Field>

				<FormMultiSelect
					form={form}
					name="probe_ids"
					label={t('maintenances.form.probes')}
					description={t('maintenances.form.probes_description')}
					options={(probes ?? []).map((probe) => ({ label: probe.name, value: probe.id }))}
				/>

				<div className="grid gap-4 sm:grid-cols-2">
					<Field>
						<FieldLabel htmlFor="starts_at">{t('maintenances.form.starts_at')}</FieldLabel>
						<Input id="starts_at" type="datetime-local" {...form.register('starts_at')} />
						<FieldError>{errors.starts_at?.message}</FieldError>
					</Field>

					<Field>
						<FieldLabel htmlFor="duration_minutes">{t('maintenances.form.duration')}</FieldLabel>
						<Input
							id="duration_minutes"
							type="number"
							min={1}
							{...form.register('duration_minutes', { valueAsNumber: true })}
						/>
						<FieldDescription>{t('maintenances.form.duration_description')}</FieldDescription>
						<FieldError>{errors.duration_minutes?.message}</FieldError>
					</Field>
				</div>

				<div className="grid gap-4 sm:grid-cols-2">
					<Field>
						<FieldLabel htmlFor="recurrence">{t('maintenances.form.recurrence')}</FieldLabel>
						<FormSelect
							form={form}
							name="recurrence"
							options={MaintenanceRecurrenceEnum.map((value) => ({
								value,
								label: t(`maintenances.recurrence.${value}`),
							}))}
						/>
						<FieldError>{errors.recurrence?.message}</FieldError>
					</Field>

					<Field>
						<FieldLabel htmlFor="timezone">{t('maintenances.form.timezone')}</FieldLabel>
						<FormSelect form={form} name="timezone" options={timezoneOptions()} />
						<FieldDescription>{t('maintenances.form.timezone_description')}</FieldDescription>
						<FieldError>{errors.timezone?.message}</FieldError>
					</Field>
				</div>

				{recurrence !== 'ONCE' && (
					<Field>
						<FieldLabel htmlFor="recurrence_until">{t('maintenances.form.recurrence_until')}</FieldLabel>
						<Input id="recurrence_until" type="datetime-local" {...form.register('recurrence_until')} />
						<FieldDescription>{t('maintenances.form.recurrence_until_description')}</FieldDescription>
						<FieldError>{errors.recurrence_until?.message}</FieldError>
					</Field>
				)}

				<FormSwitch
					form={form}
					name="active"
					label={t('maintenances.form.active')}
					description={t('maintenances.form.active_description')}
					defaultValue
				/>

				<FormSwitch
					form={form}
					name="is_public"
					label={t('maintenances.form.is_public')}
					description={t('maintenances.form.is_public_description')}
					defaultValue
				/>
			</FieldGroup>

			<DialogFooter className="mt-6">
				<Button type="submit" disabled={isLoading}>
					{t(mode === 'create' ? 'button.create' : 'button.update', { entity: t('entity.maintenance') })}
				</Button>
			</DialogFooter>
		</form>
	);
}
