import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTrigger } from '@/components/atoms/dialog.tsx';
import { Button } from '@/components/atoms/button.tsx';
import { BellPlus } from 'lucide-react';
import { DialogTitle } from '@radix-ui/react-dialog';
import useNotificationForm from '@/features/notifications/hooks/useNotificationForm.ts';
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field.tsx';
import FormSelect from '@/components/molecules/forms/form-select.tsx';
import { Input } from '@/components/atoms/input.tsx';
import { Separator } from '@/components/atoms/separator.tsx';
import FormSwitch from '@/components/molecules/forms/form-switch.tsx';
import NotificationTypeEnum from '@/features/notifications/enums/notification-type-enum.ts';
import NOTIFICATION_FIELDS_CONFIG from '@/features/notifications/components/config/notification-type.ts';
import FormFieldNotification from '@/features/notifications/components/forms/form-field-notification.tsx';
import { useTranslation } from 'react-i18next';

export default function CreateNotificationDialogue() {
	const { t } = useTranslation();
	const { openDialogue, setOpenDialogue, form, onSubmit, isLoading, errors } = useNotificationForm();
	const notificationType = form.watch('notification_type');

	const dynamicFields = notificationType
		? NOTIFICATION_FIELDS_CONFIG[notificationType]
		: NOTIFICATION_FIELDS_CONFIG[NotificationTypeEnum.DISCORD];

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		e.stopPropagation();
		form.handleSubmit(onSubmit)(e);
	};

	return (
		<Dialog open={openDialogue} onOpenChange={setOpenDialogue}>
			<DialogTrigger asChild>
				<Button>
					<BellPlus className="mr-2 h-4 w-4" />
					{t('notifications.title.create')}
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-xl">
				<form onSubmit={handleSubmit} id="create-notification-form">
					<DialogHeader>
						<DialogTitle>{t('notifications.title.create')}</DialogTitle>
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
						<Button variant="outline" type="button">
							Test notification
						</Button>

						<Button type="submit" disabled={isLoading}>
							{t(isLoading ? 'button.loading' : 'button.create', {
								entity: 'notification',
							})}
						</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	);
}
