import type { ComponentType, ReactNode } from 'react';
import type { MetricState } from '@/lib/status.ts';

interface MetricProps {
	label: string;
	value: ReactNode;
	/** Rendered smaller and muted next to the value, e.g. "ms" or "%". */
	unit?: string;
	description?: string;
	icon?: ComponentType<{ className?: string }>;
	/** Only ever set this when the value itself is the problem. Defaults to neutral. */
	state?: MetricState;
	/**
	 * `tile` stands on its own against the page background. `inline` drops the box entirely, for
	 * figures that live *inside* a card: a bordered tile inside a bordered card is a double frame,
	 * and it costs a lot of vertical space for three digits.
	 */
	variant?: 'tile' | 'inline';
	/** Sparkline, bar chart or any small visual placed under the value. */
	children?: ReactNode;
}

const valueTone: Record<MetricState, string> = {
	neutral: 'text-foreground',
	up: 'text-status-up-fg',
	degraded: 'text-status-degraded-fg',
	down: 'text-status-down-fg',
};

const iconTone: Record<MetricState, string> = {
	neutral: 'bg-muted text-muted-foreground',
	up: 'bg-status-up-bg text-status-up-fg',
	degraded: 'bg-status-degraded-bg text-status-degraded-fg',
	down: 'bg-status-down-bg text-status-down-fg',
};

/** The single stat figure of the app, in a box or bare. */
export default function Metric({
	label,
	value,
	unit,
	description,
	icon: Icon,
	state = 'neutral',
	variant = 'tile',
	children,
}: MetricProps) {
	const isTile = variant === 'tile';

	return (
		<div className={isTile ? 'bg-card border-border rounded-lg border px-4 py-3' : ''}>
			<div className="flex items-center justify-between gap-2">
				<h3 className="text-muted-foreground text-xs font-medium tracking-wide uppercase">{label}</h3>
				{Icon && isTile && (
					<span className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-lg ${iconTone[state]}`}>
						<Icon className="h-3.5 w-3.5" />
					</span>
				)}
			</div>

			<div className="mt-1 flex items-baseline gap-1.5">
				<span className={`tabular text-2xl font-semibold ${valueTone[state]}`}>{value}</span>
				{unit && <span className="text-muted-foreground text-sm font-normal">{unit}</span>}
			</div>

			{description && <p className="text-muted-foreground mt-0.5 text-xs">{description}</p>}
			{children && <div className="mt-2">{children}</div>}
		</div>
	);
}

/**
 * Lays out bare metrics in a row separated by hairlines rather than by boxes. Wraps on narrow
 * screens without leaving a metric stranded on its own line.
 */
export function MetricRow({ children }: { children: ReactNode }) {
	return (
		<div className="divide-border border-border grid grid-cols-2 gap-x-6 gap-y-4 sm:flex sm:divide-x [&>*]:sm:flex-1 [&>*:not(:first-child)]:sm:pl-6">
			{children}
		</div>
	);
}
