import { cn } from '@/lib/utils';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card';
import { Field, FieldGroup, FieldLabel } from '@/components/atoms/field';
import { Input } from '@/components/atoms/input';
import { Button } from '@/components/atoms/button.tsx';
import useLoginForm from '@/features/auth/hooks/useLoginForm.ts';
import { useTranslation } from 'react-i18next';

export function LoginForm({ className, ...props }: React.ComponentProps<'div'>) {
	const { t } = useTranslation();
	const { form, isLoading, onsubmit } = useLoginForm();

	return (
		<div className={cn('flex flex-col gap-6', className)} {...props}>
			<Card>
				<CardHeader>
					<CardTitle className="text-center text-xl">{t('pages.login.title')}</CardTitle>
					<CardDescription className="text-center">{t('pages.login.description')}</CardDescription>
				</CardHeader>
				<CardContent>
					<form onSubmit={form.handleSubmit(onsubmit)}>
						<FieldGroup>
							<Field>
								<FieldLabel htmlFor="email">{t('form.label.email')}</FieldLabel>
								<Input
									{...form.register('email')}
									id="email"
									type="email"
									placeholder={t('form.placeholder.email')}
									required
								/>
							</Field>
							<Field>
								<div className="flex items-center">
									<FieldLabel htmlFor="password">{t('form.label.password')}</FieldLabel>
								</div>
								<Input
									{...form.register('password')}
									id="password"
									type="password"
									placeholder={t('form.placeholder.password')}
									required
								/>
							</Field>
							<Field>
								<Button type="submit" disabled={isLoading} className="w-full">
									{t(isLoading ? 'button.loading' : 'button.login')}
								</Button>
							</Field>
						</FieldGroup>
					</form>
				</CardContent>
			</Card>
		</div>
	);
}
