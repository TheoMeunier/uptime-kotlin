import useUpdateProfile from '@/features/profile/hooks/useUpdateProfile';
import { Button } from '@/components/atoms/button';
import { Input } from '@/components/atoms/input';
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/atoms/card';
import { useTranslation } from 'react-i18next';

export default function UpdateProfileForm() {
	const { t } = useTranslation();
	const { form, isLoading, onSubmit, errors } = useUpdateProfile();

	return (
		<form onSubmit={form.handleSubmit(onSubmit)}>
			<Card>
				<CardHeader>
					<CardTitle>{t('profile.title.update_profile')}</CardTitle>
					<CardDescription>{t('profile.description.update_profile')}</CardDescription>
				</CardHeader>

				<CardContent>
					<FieldGroup>
						<Field>
							<FieldLabel htmlFor="name">{t('form.label.username')}</FieldLabel>
							<Input {...form.register('name')} id="name" type="text" autoComplete="name" required />
							<FieldError>{errors.name?.message}</FieldError>
						</Field>

						<Field>
							<FieldLabel htmlFor="email">{t('form.label.email')}</FieldLabel>
							<Input {...form.register('email')} id="email" type="email" autoComplete="email" required />
							<FieldError>{errors.email?.message}</FieldError>
						</Field>
					</FieldGroup>
				</CardContent>

				<CardFooter>
					<Button type="submit" disabled={isLoading}>
						{isLoading ? t('button.saving') : t('button.save', { entity: t('entity.profile') })}
					</Button>
				</CardFooter>
			</Card>
		</form>
	);
}
