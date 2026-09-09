import { Button } from '@/components/atoms/button.tsx';
import { Input } from '@/components/atoms/input.tsx';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field.tsx';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import useUpdatePasswordProfile from '@/features/profile/hooks/useUpdatePasswordProfile.ts';
import { useTranslation } from 'react-i18next';

export default function UpdatePasswordProfileForm() {
	const { t } = useTranslation();
	const { form, isLoading, onSubmit, errors } = useUpdatePasswordProfile();

	return (
		<form onSubmit={form.handleSubmit(onSubmit)}>
			<Card>
				<CardHeader>
					<CardTitle>{t('profile.title.update_password')}</CardTitle>
					<CardDescription>{t('profile.description.update_password')}</CardDescription>
				</CardHeader>

				<CardContent>
					<FieldGroup>
						<Field>
							<FieldLabel htmlFor="password">{t('profile.label.password')}</FieldLabel>
							<Input
								{...form.register('password')}
								id="password"
								type="password"
								autoComplete="new-password"
								required
							/>
							<FieldDescription>{t('form.description.password')}</FieldDescription>
							<FieldError>{errors.password?.message}</FieldError>
						</Field>

						<Field>
							<FieldLabel htmlFor="password_confirmation">{t('profile.label.password_confirm')}</FieldLabel>
							<Input
								{...form.register('password_confirmation')}
								id="password_confirmation"
								type="password"
								autoComplete="new-password"
								required
							/>
							<FieldError>{errors.password_confirmation?.message}</FieldError>
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
