import { useTranslation } from 'react-i18next';
import { DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/atoms/dialog.tsx';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field.tsx';
import { Input } from '@/components/atoms/input.tsx';
import { Button } from '@/components/atoms/button.tsx';
import FormSelect from '@/components/molecules/forms/form-select.tsx';
import useUserForm, { type UserFormValues } from '@/features/users/hooks/useUserForm.ts';
import { UserRoleEnum } from '@/features/users/schemas/user.schema.ts';

interface UserFormProps {
	onSubmit: (values: UserFormValues) => void;
	isLoading?: boolean;
}

export default function UserForm({ onSubmit, isLoading }: UserFormProps) {
	const { t } = useTranslation();
	const { form, errors } = useUserForm();
	const role = form.watch('role');

	return (
		<form onSubmit={form.handleSubmit(onSubmit)} noValidate>
			<DialogHeader>
				<DialogTitle>{t('users.title.create')}</DialogTitle>
				<DialogDescription>{t('users.description.create')}</DialogDescription>
			</DialogHeader>

			<FieldGroup className="mt-4">
				<Field>
					<FieldLabel htmlFor="user-name">{t('form.label.full_name')}</FieldLabel>
					<Input id="user-name" autoComplete="off" aria-invalid={Boolean(errors.name)} {...form.register('name')} />
					<FieldError>{errors.name?.message}</FieldError>
				</Field>

				<Field>
					<FieldLabel htmlFor="user-email">{t('form.label.email')}</FieldLabel>
					<Input
						id="user-email"
						type="email"
						autoComplete="off"
						aria-invalid={Boolean(errors.email)}
						{...form.register('email')}
					/>
					<FieldError>{errors.email?.message}</FieldError>
				</Field>

				<Field>
					<FieldLabel htmlFor="user-password">{t('users.form.password')}</FieldLabel>
					<Input
						id="user-password"
						type="password"
						autoComplete="new-password"
						aria-invalid={Boolean(errors.password)}
						{...form.register('password')}
					/>
					<FieldDescription>{t('users.form.password_description')}</FieldDescription>
					<FieldError>{errors.password?.message}</FieldError>
				</Field>

				<Field>
					<FieldLabel>{t('users.form.role')}</FieldLabel>
					<FormSelect
						form={form}
						name="role"
						options={UserRoleEnum.map((value) => ({ value, label: t(`users.role.${value}`) }))}
					/>
					<FieldDescription>{t(`users.role_description.${role}`)}</FieldDescription>
				</Field>
			</FieldGroup>

			<DialogFooter className="mt-6">
				<Button type="submit" disabled={isLoading}>
					{isLoading ? t('button.saving') : t('users.actions.create')}
				</Button>
			</DialogFooter>
		</form>
	);
}
