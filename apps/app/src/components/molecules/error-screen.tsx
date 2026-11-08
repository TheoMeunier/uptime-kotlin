import { useTranslation } from 'react-i18next';
import { LayoutDashboard, LoaderCircle, RefreshCw, RotateCw } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Card, CardContent } from '@/components/atoms/card.tsx';
import { cn } from '@/lib/utils.ts';
import ErrorPage from '@/components/molecules/error-page.tsx';

interface ErrorScreenProps {
	kind: 'chunk' | 'crash';
	reloading?: boolean;
	error?: unknown;
	fullscreen?: boolean;
}

function errorDetails(error: unknown): string | undefined {
	if (error instanceof Error) return error.stack ?? `${error.name}: ${error.message}`;
	if (error === undefined || error === null) return undefined;
	try {
		return typeof error === 'string' ? error : JSON.stringify(error, null, 2);
	} catch {
		return String(error);
	}
}

export default function ErrorScreen({ kind, reloading = false, error, fullscreen = false }: ErrorScreenProps) {
	const { t } = useTranslation();
	const details = kind === 'crash' ? errorDetails(error) : undefined;

	const Icon = reloading ? LoaderCircle : RefreshCw;
	const description = reloading ? t('errors.chunk.reloading') : t('errors.chunk.stalled');

	if (kind === 'crash') {
		return (
			<div role="alert" className="w-full">
				<ErrorPage
					code="500"
					fullscreen={fullscreen}
					title={t('errors.crash.title')}
					description={t('errors.crash.description')}
					actions={
						<>
							<Button onClick={() => window.location.reload()}>
								<RotateCw className="size-4" />
								{t('errors.actions.reload')}
							</Button>
							<Button variant="outline" asChild>
								<a href="/dashboard">
									<LayoutDashboard className="size-4" />
									{t('errors.actions.dashboard')}
								</a>
							</Button>
						</>
					}
				>
					{details && (
						<details className="mt-10 w-full text-left">
							<summary className="text-muted-foreground cursor-pointer text-sm">{t('errors.crash.details')}</summary>
							<pre className="bg-muted mt-3 max-h-64 overflow-auto rounded-md p-3 font-mono text-xs whitespace-pre-wrap">
								{details}
							</pre>
						</details>
					)}
				</ErrorPage>
			</div>
		);
	}

	return (
		<div
			className={cn('flex w-full items-center justify-center', fullscreen && 'bg-background min-h-svh p-4')}
			role="alert"
		>
			<Card className="w-full max-w-lg">
				<CardContent className="flex flex-col items-center py-12 text-center">
					<span className="bg-muted text-foreground mb-4 flex size-12 items-center justify-center rounded-full">
						<Icon className={cn('size-6', reloading && 'animate-spin')} />
					</span>

					<h2 className="text-base font-semibold">{t('errors.chunk.title')}</h2>
					<p className="text-muted-foreground mt-1 max-w-sm text-sm">{description}</p>

					{!reloading && (
						<Button variant="outline" className="mt-5" onClick={() => window.location.reload()}>
							<RotateCw className="mr-2 size-4" />
							{t('errors.actions.reload')}
						</Button>
					)}
				</CardContent>
			</Card>
		</div>
	);
}
