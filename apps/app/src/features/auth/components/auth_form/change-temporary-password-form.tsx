import { useTranslation } from 'react-i18next';
import { KeyRound } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/atoms/field';
import { Input } from '@/components/atoms/input';
import { Button } from '@/components/atoms/button.tsx';
import useChangeTemporaryPassword from '@/features/auth/hooks/useChangeTemporaryPassword.ts';

export function ChangeTemporaryPasswordForm() {
	const { t } = useTranslation();
	const { form, onSubmit, isLoading, errors, askTemporaryPassword, signOut } = useChangeTemporaryPassword();

	return (
		<Card>
			<CardHeader className="items-center text-center">
				<span className="bg-muted mx-auto mb-2 flex size-12 items-center justify-center rounded-full">
					<KeyRound className="size-6" />
				</span>
				<CardTitle className="text-xl">{t('pages.change_password.title')}</CardTitle>
				<CardDescription>{t('pages.change_password.description')}</CardDescription>
			</CardHeader>
			<CardContent>
				<form onSubmit={form.handleSubmit(onSubmit)}>
					<FieldGroup>
						{askTemporaryPassword ? (
							<Field>
								<FieldLabel htmlFor="current_password">{t('pages.change_password.temporary')}</FieldLabel>
								<Input
									{...form.register('current_password')}
									id="current_password"
									type="password"
									autoComplete="current-password"
									required
								/>
								<FieldError>{errors.current_password?.message}</FieldError>
							</Field>
						) : (
							<input type="hidden" {...form.register('current_password')} />
						)}

						<Field>
							<FieldLabel htmlFor="password">{t('profile.label.password')}</FieldLabel>
							<Input
								{...form.register('password')}
								id="password"
								type="password"
								autoComplete="new-password"
								autoFocus
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

						<Field>
							<Button type="submit" disabled={isLoading} className="w-full">
								{isLoading ? t('button.saving') : t('pages.change_password.submit')}
							</Button>
							<Button type="button" variant="ghost" className="w-full" onClick={() => void signOut()}>
								{t('pages.change_password.sign_out')}
							</Button>
						</Field>
					</FieldGroup>
				</form>
			</CardContent>
		</Card>
	);
}
