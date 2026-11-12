import { ChangeTemporaryPasswordForm } from '@/features/auth/components/auth_form/change-temporary-password-form.tsx';
import LanguageToggle from '@/components/molecules/language-toggle.tsx';

export default function ChangePassword() {
	return (
		<div className="bg-background relative flex min-h-svh flex-col items-center justify-center gap-6 p-6 md:p-10">
			<div className="absolute top-4 right-4">
				<LanguageToggle />
			</div>

			<div className="flex w-full max-w-sm flex-col gap-6">
				<span className="flex items-center gap-2 self-center font-medium">
					<div className="text-primary-foreground flex size-6 items-center justify-center rounded-md">
						<img src="/img/logo-ui.png" alt="" />
					</div>
					Uptime Kotlin
				</span>
				<ChangeTemporaryPasswordForm />
			</div>
		</div>
	);
}
