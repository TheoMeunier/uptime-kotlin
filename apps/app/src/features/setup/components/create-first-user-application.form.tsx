import useStoreFirstUserApplication from '@/features/setup/hooks/useFirstUserApplicationForm.ts';
import { cn } from '@/lib/utils';
import { Button } from '@/components/atoms/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field';
import { Input } from '@/components/atoms/input';
import { useTranslation } from 'react-i18next';

export default function CreateFirstUserApplicationForm() {
	const { t } = useTranslation();
	const { form, isLoading, errors, onSubmit } = useStoreFirstUserApplication();

	return (
		<div className={cn('flex flex-col gap-6')}>
			<Card>
				<CardHeader className="text-center">
					<CardTitle className="text-xl">{t('profile.title.create_first_user')}</CardTitle>
					<CardDescription>{t('profile.description.create_first_user')}</CardDescription>
				</CardHeader>
				<CardContent>
					<form onSubmit={form.handleSubmit(onSubmit)}>
						<FieldGroup>
							<Field>
								<Field>
									<FieldLabel htmlFor="name">{t('form.label.full_name')}</FieldLabel>
									<Input {...form.register('name')} type="text" placeholder="John Doe" required />
									<FieldError>{errors.name?.message}</FieldError>
								</Field>
								<Field>
									<FieldLabel htmlFor="email">{t('form.label.email')}</FieldLabel>
									<Input {...form.register('email')} type="email" placeholder="admin@uptime-kotlin.com" required />
									<FieldError>{errors.email?.message}</FieldError>
								</Field>
								<Field>
									<FieldLabel htmlFor="password">{t('form.label.password')}</FieldLabel>
									<Input {...form.register('password')} type="password" required />
									<FieldError>{errors.password?.message}</FieldError>
								</Field>
								<Field>
									<FieldLabel htmlFor="confirm-password">{t('form.label.confirmation_password')}</FieldLabel>
									<Input {...form.register('password_confirmation')} type="password" required />
									<FieldError>{errors.password_confirmation?.message}</FieldError>
								</Field>
								<FieldDescription>{t('form.description.password')}</FieldDescription>
							</Field>
							<Field>
								<Button type="submit" disabled={isLoading}>
									{t('button.create', { entity: 'first account' })}
								</Button>
							</Field>
						</FieldGroup>
					</form>
				</CardContent>
			</Card>
		</div>
	);
}
