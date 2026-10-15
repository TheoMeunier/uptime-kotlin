import '@/features/demo/setup.ts';
import { useEffect } from 'react';
import { Eye } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import useDemoCredentials, { type DemoCredentials } from '@/features/demo/hooks/useDemoCredentials.ts';
import { forgetOnboardingCompletion } from '@/features/onboarding/hooks/useOnboardingTour.ts';

type DemoLoginHintProps = {
	onCredentials: (credentials: DemoCredentials) => void;
};

export default function DemoLoginHint({ onCredentials }: DemoLoginHintProps) {
	const { t } = useTranslation();
	const { data: credentials } = useDemoCredentials();

	useEffect(() => {
		if (!credentials) return;

		onCredentials(credentials);
		forgetOnboardingCompletion(credentials.email);
	}, [credentials, onCredentials]);

	if (!credentials) return null;

	return (
		<div className="bg-muted/40 flex gap-3 rounded-lg border border-dashed p-4 text-sm">
			<Eye className="text-muted-foreground mt-0.5 size-4 shrink-0" aria-hidden />
			<div className="min-w-0 space-y-1">
				<p className="font-medium">{t('demo.login_title')}</p>
				<p className="text-muted-foreground">{t('demo.login_hint')}</p>
				<dl className="grid grid-cols-[auto_1fr] gap-x-3 gap-y-0.5 pt-1 font-mono text-xs">
					<dt className="text-muted-foreground">{t('form.label.email')}</dt>
					<dd className="truncate">{credentials.email}</dd>
					<dt className="text-muted-foreground">{t('form.label.password')}</dt>
					<dd className="truncate">{credentials.password}</dd>
				</dl>
			</div>
		</div>
	);
}
