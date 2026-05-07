import useUpdateProfile from '@/features/profile/hooks/useUpdateProfile';
import { Button } from '@/components/atoms/button';
import { Input } from '@/components/atoms/input';
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field';
import { useTranslation } from 'react-i18next';

export default function UpdateProfileForm() {
	const { t } = useTranslation();
	const { form, isLoading, onSubmit, errors } = useUpdateProfile();

	return (
		<div className="w-full max-w-2xl space-y-6">
			<div className="space-y-1">
				<h2 className="text-xl font-semibold">{t('profile.title.update_profile')}</h2>
				<p className="text-sm text-muted-foreground">{t('profile.description.update_profile')}</p>
			</div>

			<form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
				<FieldGroup>
					<Field>
						<FieldLabel htmlFor="name">{t('form.label.username')}</FieldLabel>
						<Input {...form.register('name')} id="name" type="text" required />
						<FieldError>{errors.name?.message}</FieldError>
					</Field>

					<Field>
						<FieldLabel htmlFor="email">{t('form.label.email')}</FieldLabel>
						<Input {...form.register('email')} id="email" type="email" required />
						<FieldError>{errors.email?.message}</FieldError>
					</Field>
				</FieldGroup>

				<div>
					<Button type="submit" disabled={isLoading}>
						{isLoading ? t('button.saving') : t('button.save', { entity: t('entity.profile') })}
					</Button>
				</div>
			</form>
		</div>
	);
}
