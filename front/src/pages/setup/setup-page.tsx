import CreateFirstUserApplicationForm from '@/features/setup/components/create-first-user-application.form.tsx';
import useSetup from '@/features/setup/hooks/useSetupApp.ts';
import LoaderPage from '@/features/setup/components/loader-page.tsx';
import { Navigate } from 'react-router';

export default function SetupPage() {
	const { isSetupComplete, isLoading } = useSetup();

	if (isLoading) return <LoaderPage />;

	if (isSetupComplete) {
		return <Navigate to="/dashboard" replace />;
	}

	return (
		<div className="bg-muted flex min-h-svh flex-col items-center justify-center gap-6 p-6 md:p-10">
			<div className="flex w-full max-w-sm flex-col gap-6">
				<div className="flex items-center gap-2 self-center font-medium">
					<div className="text-primary-foreground flex size-6 items-center justify-center rounded-md">
						<img src="/img/logo.png" alt="logo" />
					</div>
					Uptime Kotlin
				</div>
				<CreateFirstUserApplicationForm />
			</div>
		</div>
	);
}
