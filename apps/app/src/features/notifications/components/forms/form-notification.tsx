import { useEffect, useMemo, useRef } from 'react';
import { cn } from '@/lib/utils';
import { toast } from 'sonner';
import { DialogFooter, DialogHeader, DialogTitle } from '@/components/atoms/dialog.tsx';
import { useTranslation } from 'react-i18next';
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field.tsx';
import FormSelect from '@/components/molecules/forms/form-select.tsx';
import NotificationTypeEnum, {
	NOTIFICATION_TYPE_GROUPS,
	NOTIFICATION_TYPE_LABELS,
} from '@/features/notifications/enums/notification-type-enum.ts';
import { Input } from '@/components/atoms/input.tsx';
import { buildNotificationFieldsConfig } from '@/features/notifications/components/config/notification-type.ts';
import FormFieldNotification from '@/features/notifications/components/forms/form-field-notification.tsx';
import FormSwitch from '@/components/molecules/forms/form-switch.tsx';
import { Button } from '@/components/atoms/button.tsx';
import type { FieldConfig } from '@/features/notifications/types/notification.type.ts';
import useNotificationTesting from '@/features/notifications/hooks/useNotificationTesting.ts';
import useNotificationForm, {
	type NotificationFormMode,
	type StoreNotificationSchema,
	withEmptyFieldsAsNull,
} from '@/features/notifications/hooks/useNotificationForm.ts';

interface NotificationFormProps {
	mode: NotificationFormMode;
	defaultValues: Partial<StoreNotificationSchema>;
	onSubmit: (values: StoreNotificationSchema) => void;
	isLoading?: boolean;
}

export default function FormNotification({ mode, defaultValues, isLoading, onSubmit }: NotificationFormProps) {
	const { t } = useTranslation();
	const { isTesting, testNotification } = useNotificationTesting();
	const { form, errors } = useNotificationForm({ defaultValues, mode });

	const notificationType = form.watch('notification_type');

	const NOTIFICATION_FIELDS_CONFIG = useMemo(() => buildNotificationFieldsConfig(t), [t]);

	const suggestedName = t('notifications.placeholder.notification_name', {
		type: NOTIFICATION_TYPE_LABELS[notificationType ?? NotificationTypeEnum.DISCORD],
	});

	const lastSuggestedName = useRef<string | undefined>(undefined);
	useEffect(() => {
		if (mode !== 'create') return;
		const currentName = form.getValues('name');
		if (!currentName || currentName === lastSuggestedName.current) {
			form.setValue('name', suggestedName, { shouldDirty: true, shouldValidate: false });
		}
		lastSuggestedName.current = suggestedName;
	}, [mode, suggestedName, form]);

	const previousType = useRef(notificationType);
	useEffect(() => {
		if (mode !== 'create' || !notificationType || previousType.current === notificationType) return;
		previousType.current = notificationType;

		NOTIFICATION_FIELDS_CONFIG[notificationType]?.forEach((field: FieldConfig) => {
			form.setValue(field.name as never, field.default_value as never, {
				shouldDirty: true,
				shouldValidate: false,
			});
		});
	}, [mode, notificationType, NOTIFICATION_FIELDS_CONFIG, form]);

	const mailSecurity = form.watch('security' as never) as unknown as string | undefined;
	const previousSecurity = useRef(mailSecurity);
	useEffect(() => {
		const previous = previousSecurity.current;
		previousSecurity.current = mailSecurity;
		if (notificationType !== NotificationTypeEnum.MAIL || !previous || previous === mailSecurity) return;

		const port = Number(form.getValues('port' as never));
		if (port && ![25, 465, 587].includes(port)) return;
		const next = mailSecurity === 'SMTPS' ? 465 : mailSecurity === 'STARTTLS' ? 587 : undefined;
		if (next && next !== port) {
			form.setValue('port' as never, next as never, { shouldDirty: true });
		}
	}, [notificationType, mailSecurity, form]);

	const dynamicFields: FieldConfig[] = (
		notificationType
			? NOTIFICATION_FIELDS_CONFIG[notificationType]
			: NOTIFICATION_FIELDS_CONFIG[NotificationTypeEnum.DISCORD]
	).filter((field: FieldConfig) => mode === 'update' || !field.update_only);

	const fieldSections = (dynamicFields ?? []).reduce<{ title?: string; fields: FieldConfig[] }[]>((acc, field) => {
		const last = acc[acc.length - 1];
		if (last && last.title === field.section) last.fields.push(field);
		else acc.push({ title: field.section, fields: [field] });
		return acc;
	}, []);

	const SPAN_CLASSES: Record<NonNullable<FieldConfig['span']>, string> = {
		half: 'sm:col-span-3',
		wide: 'sm:col-span-4',
		narrow: 'sm:col-span-2',
	};

	const handleTestNotifications = (e: React.FormEvent) => {
		e.preventDefault();
		e.stopPropagation();

		const isValid = form.trigger();
		if (!isValid) return null;

		const formData = form.getValues();
		const currentFieldNames = dynamicFields?.map((f) => f.name) ?? [];

		const filteredData = {
			notification_type: formData.notification_type,
			name: formData.name,
			is_default: formData.is_default,
			...Object.fromEntries(Object.entries(formData).filter(([key]) => currentFieldNames.includes(key))),
		} as StoreNotificationSchema;

		testNotification(withEmptyFieldsAsNull(filteredData));
	};

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		e.stopPropagation();

		form.handleSubmit(
			(values) => onSubmit(withEmptyFieldsAsNull(values)),
			() => toast.error(t('notifications.alerts.invalid'))
		)(e);
	};

	return (
		<form
			onSubmit={handleSubmit}
			id="update-notification-form"
			className="flex max-h-[calc(100dvh-5.5rem)] min-h-0 flex-col"
		>
			<DialogHeader className="shrink-0">
				<DialogTitle>
					{mode === 'create' ? t('notifications.title.create') : t('notifications.title.update')}
				</DialogTitle>
			</DialogHeader>

			<div className="-mx-1 mt-4 min-h-0 flex-1 space-y-6 overflow-y-auto px-1 pb-1">
				<div className="grid gap-4 sm:grid-cols-2">
					<Field>
						<FieldLabel htmlFor="notification_type">{t('notifications.label.type_notification')}</FieldLabel>
						<FormSelect
							form={form}
							name="notification_type"
							options={NOTIFICATION_TYPE_GROUPS.map((group) => ({
								label: t(group.labelKey),
								options: group.types.map((type) => ({
									value: type,
									label: NOTIFICATION_TYPE_LABELS[type],
								})),
							}))}
						/>
						<FieldError>{errors.notification_type?.message}</FieldError>
					</Field>
					<Field>
						<FieldLabel htmlFor="name">{t('notifications.label.notification_name')}</FieldLabel>
						<Input {...form.register('name')} id="name" placeholder={suggestedName} required />
						<FieldError>{errors.name?.message}</FieldError>
					</Field>
				</div>

				{notificationType &&
					fieldSections.map((section, index) =>
						section.title ? (
							<section key={section.title} className="space-y-3">
								<h3 className="text-muted-foreground border-b pb-1 text-xs font-semibold tracking-wide uppercase">
									{section.title}
								</h3>
								<div className="grid gap-4 sm:grid-cols-6">
									{section.fields.map((field) => (
										<div key={field.name} className={cn('col-span-full', field.span && SPAN_CLASSES[field.span])}>
											<FormFieldNotification field={field} form={form} />
										</div>
									))}
								</div>
							</section>
						) : (
							<FieldGroup key={`fields-${index}`}>
								{section.fields.map((field) => (
									<FormFieldNotification key={field.name} field={field} form={form} />
								))}
							</FieldGroup>
						)
					)}

				<FieldGroup>
					<div className="flex items-center space-x-2">
						<FormSwitch form={form} name={'is_default'} label={t('notifications.label.set_as_default')} />
						<FieldError>{errors.is_default?.message}</FieldError>
					</div>
				</FieldGroup>
			</div>

			<DialogFooter className="mt-4 flex w-full shrink-0 items-center justify-between gap-4 border-t pt-4">
				<Button variant="outline" type="button" onClick={handleTestNotifications}>
					{t(isTesting ? 'button.loading' : 'button.test', {
						entity: t('entity.notification'),
					})}
				</Button>

				<Button type="submit" disabled={isLoading}>
					{t(isLoading ? 'button.loading' : mode === 'create' ? 'button.create' : 'button.update', {
						entity: t('entity.notification'),
					})}
				</Button>
			</DialogFooter>
		</form>
	);
}
