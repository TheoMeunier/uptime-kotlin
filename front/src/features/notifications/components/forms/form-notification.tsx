import { DialogFooter, DialogHeader, DialogTitle } from '@/components/atoms/dialog.tsx';
import { useTranslation } from 'react-i18next';
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field.tsx';
import FormSelect from '@/components/molecules/forms/form-select.tsx';
import NotificationTypeEnum from '@/features/notifications/enums/notification-type-enum.ts';
import { Input } from '@/components/atoms/input.tsx';
import NOTIFICATION_FIELDS_CONFIG from '@/features/notifications/components/config/notification-type.ts';
import FormFieldNotification from '@/features/notifications/components/forms/form-field-notification.tsx';
import { Separator } from '@radix-ui/react-select';
import FormSwitch from '@/components/molecules/forms/form-switch.tsx';
import { Button } from '@/components/atoms/button.tsx';
import useNotificationTesting from '@/features/notifications/hooks/useNotificationTesting.ts';
import useNotificationForm, {
	type NotificationFormMode,
	type StoreNotificationSchema,
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

	const dynamicFields = notificationType
		? NOTIFICATION_FIELDS_CONFIG[notificationType]
		: NOTIFICATION_FIELDS_CONFIG[NotificationTypeEnum.DISCORD];

	const handleTestNotifications = (e: React.FormEvent) => {
		e.preventDefault();
		e.stopPropagation();

		const isValid = form.trigger();

		if (!isValid) return null;

		const formData = form.getValues();
		testNotification(formData);
	};

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		e.stopPropagation();
		form.handleSubmit(onSubmit)(e);
	};

	return (
		<form onSubmit={handleSubmit} id="update-notification-form">
			<DialogHeader>
				<DialogTitle>
					{mode === 'create' ? t('notifications.title.create') : t('notifications.title.update')}
				</DialogTitle>
			</DialogHeader>

			<div className="mt-4 space-y-4">
				<FieldGroup>
					<Field>
						<FieldLabel htmlFor="notification_type">{t('notifications.label.type_notification')}</FieldLabel>
						<FormSelect form={form} name="notification_type" options={Object.values(NotificationTypeEnum)} />
						<FieldError>{errors.notification_type?.message}</FieldError>
					</Field>
					<Field>
						<FieldLabel htmlFor="name">{t('notifications.label.notification_name')}</FieldLabel>
						<Input
							{...form.register('name')}
							id="name"
							placeholder={t('notifications.placeholder.notification_name')}
							required
						/>
					</Field>
					<FieldError>{errors.name?.message}</FieldError>
				</FieldGroup>

				{notificationType && dynamicFields && dynamicFields.length > 0 && (
					<FieldGroup>
						{dynamicFields.map((field) => (
							<FormFieldNotification key={field.name} field={field} form={form} />
						))}
					</FieldGroup>
				)}

				<Separator className="mb-4" />

				<FieldGroup>
					<div className="flex items-center space-x-2">
						<FormSwitch form={form} name={'is_default'} label="Have the notification by default" />
						<FieldError>{errors.is_default?.message}</FieldError>
					</div>
				</FieldGroup>
			</div>

			<DialogFooter className="flex justify-between items-center gap-4 w-full mt-4">
				<Button variant="outline" type="button" onClick={handleTestNotifications}>
					{t(isTesting ? 'button.loading' : 'button.test', {
						entity: 'notification',
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
