import CreateFirstUserApplicationForm from '@/features/setup/components/create-first-user-application.form.tsx';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';

export default function SetupPage() {
	const navigate = useNavigate();
	const [isChecking, setIsChecking] = useState<boolean>(true);

	useEffect(() => {
		const isSetupComplete = localStorage.getItem('setupCompleted') === 'true';
		console.log(isSetupComplete);

		if (isSetupComplete) {
			navigate('/dashboard', { replace: true });
		} else {
			setIsChecking(false);
		}
	}, [navigate]);

	if (isChecking) {
		return null;
	}

	if (isChecking) return <div>...loading</div>;

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
