import { useTranslation } from 'react-i18next';

export default function LoaderPage() {
	const { t } = useTranslation();

	return (
		<div className="bg-background flex min-h-screen items-center justify-center">
			<div className="flex flex-col items-center gap-6">
				<img src="/img/logo.png" alt="" className="h-24 w-24 md:h-32 md:w-32" />

				<h1 className="text-foreground text-3xl font-semibold tracking-tight md:text-4xl">Uptime Kotlin</h1>

				<div className="bg-muted h-1.5 w-56 overflow-hidden rounded-full">
					<div className="bg-primary motion-safe:animate-loader-sweep h-full w-1/3 rounded-full" />
				</div>

				<span className="sr-only">{t('app.loading')}</span>
			</div>
		</div>
	);
}
