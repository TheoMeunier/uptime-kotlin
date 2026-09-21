import { useTranslation } from 'react-i18next';
import { LayoutDashboard, LoaderCircle, RefreshCw, RotateCw, TriangleAlert } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Card, CardContent } from '@/components/atoms/card.tsx';
import { cn } from '@/lib/utils.ts';

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

	const Icon = kind === 'chunk' ? (reloading ? LoaderCircle : RefreshCw) : TriangleAlert;
	const description =
		kind === 'chunk'
			? reloading
				? t('errors.chunk.reloading')
				: t('errors.chunk.stalled')
			: t('errors.crash.description');

	return (
		<div
			className={cn('flex w-full items-center justify-center', fullscreen && 'bg-background min-h-svh p-4')}
			role="alert"
		>
			<Card className="w-full max-w-lg">
				<CardContent className="flex flex-col items-center py-12 text-center">
					<span
						className={cn(
							'mb-4 flex size-12 items-center justify-center rounded-full',
							kind === 'chunk' ? 'bg-muted text-foreground' : 'bg-status-down-bg text-status-down-fg'
						)}
					>
						<Icon className={cn('size-6', reloading && 'animate-spin')} />
					</span>

					<h2 className="text-base font-semibold">
						{kind === 'chunk' ? t('errors.chunk.title') : t('errors.crash.title')}
					</h2>
					<p className="text-muted-foreground mt-1 max-w-sm text-sm">{description}</p>

					{!reloading && (
						<div className="mt-5 flex flex-wrap justify-center gap-2">
							<Button variant="outline" onClick={() => window.location.reload()}>
								<RotateCw className="mr-2 size-4" />
								{t('errors.actions.reload')}
							</Button>
							{kind === 'crash' && (
								<Button variant="ghost" asChild>
									<a href="/dashboard">
										<LayoutDashboard className="mr-2 size-4" />
										{t('errors.actions.dashboard')}
									</a>
								</Button>
							)}
						</div>
					)}

					{details && (
						<details className="mt-6 w-full text-left">
							<summary className="text-muted-foreground cursor-pointer text-xs">{t('errors.crash.details')}</summary>
							<pre className="bg-muted mt-2 max-h-48 overflow-auto rounded-md p-3 text-xs whitespace-pre-wrap">
								{details}
							</pre>
						</details>
					)}
				</CardContent>
			</Card>
		</div>
	);
}
