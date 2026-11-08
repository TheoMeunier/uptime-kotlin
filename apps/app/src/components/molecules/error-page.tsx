import { cn } from '@/lib/utils.ts';

type ErrorCode = '404' | '500';

interface ErrorPageProps {
	code: ErrorCode;
	title: string;
	description: string;
	actions?: React.ReactNode;
	children?: React.ReactNode;
	fullscreen?: boolean;
}

const BAR_COUNT = 36;

type BarState = 'up' | 'degraded' | 'down' | 'empty';

function trackFor(code: ErrorCode): BarState[] {
	return Array.from({ length: BAR_COUNT }, (_, index) => {
		const fromEnd = BAR_COUNT - index;
		if (code === '404') return fromEnd <= 7 ? 'empty' : 'up';
		if (fromEnd <= 5) return 'down';
		if (fromEnd <= 7) return 'degraded';
		return 'up';
	});
}

const BAR_CLASSES: Record<BarState, string> = {
	up: 'bg-status-up',
	degraded: 'bg-status-degraded',
	down: 'bg-status-down',
	empty: 'border-muted-foreground/40 border border-dashed bg-transparent',
};

function UptimeTrack({ code }: { code: ErrorCode }) {
	const track = trackFor(code);

	return (
		<div aria-hidden="true" className="flex h-10 items-end gap-[3px]">
			{track.map((state, index) => (
				<span
					key={index}
					className={cn(
						'h-full min-w-0 flex-1 rounded-[2px]',
						BAR_CLASSES[state],
						code === '500' && index === track.length - 1 && 'animate-error-pulse'
					)}
				/>
			))}
		</div>
	);
}

export default function ErrorPage({ code, title, description, actions, children, fullscreen = true }: ErrorPageProps) {
	return (
		<main
			className={cn(
				'bg-background flex w-full items-center justify-center px-4 sm:px-6',
				fullscreen ? 'min-h-svh py-16' : 'min-h-[70vh] py-12'
			)}
		>
			<div className="w-full max-w-xl">
				<UptimeTrack code={code} />

				<p
					className={cn(
						'mt-8 font-mono text-7xl leading-none font-semibold tracking-tight sm:text-8xl',
						code === '404' ? 'text-muted-foreground' : 'text-status-down-fg'
					)}
				>
					{code}
				</p>

				<h1 className="text-foreground mt-6 text-2xl font-semibold tracking-tight text-balance">{title}</h1>
				<p className="text-muted-foreground mt-2 max-w-prose text-base text-pretty">{description}</p>

				{actions && <div className="mt-8 flex flex-wrap gap-3">{actions}</div>}

				{children}
			</div>
		</main>
	);
}
