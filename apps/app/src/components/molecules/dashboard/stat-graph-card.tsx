import { lazy, Suspense } from 'react';
import { CardTitle } from '@/components/atoms/card.tsx';

const Sparkline = lazy(() => import('@/components/molecules/dashboard/sparkline.tsx'));

interface CardStatsProps {
	title: string;
	value: number | string | undefined;
	description?: string;
	icon: React.ComponentType<{ className?: string }>;
	variant?: 'success' | 'danger' | 'warning' | 'info' | 'neutral';
	sparklineColor?: string;
	sparklineType?: 'line' | 'bar';
	sparklineData?: { bucket: Date; value: number }[] | { value: number }[];
}

export default function StatGraphCard({
	title,
	value,
	description,
	icon: Icon,
	variant = 'neutral',
	sparklineColor,
	sparklineType,
	sparklineData,
}: CardStatsProps) {
	const iconVariants: Record<string, string> = {
		success: 'bg-green-50 text-green-700',
		danger: 'bg-red-50 text-red-700',
		warning: 'bg-orange-50 text-orange-700',
		info: 'bg-blue-50 text-blue-700',
		neutral: 'bg-muted text-muted-foreground',
	};

	const valueVariants: Record<string, string> = {
		success: 'text-green-600',
		danger: 'text-red-600',
		warning: 'text-orange-500',
		info: 'text-blue-700',
		neutral: 'text-foreground',
	};

	return (
		<div className="bg-gray-50 border border-gray-100 rounded-lg py-3 px-4">
			<div className="flex flex-row items-center justify-between mb-2">
				<CardTitle className="text-xs font-medium text-muted-foreground uppercase tracking-wide">{title}</CardTitle>
				<div className={`h-7 w-7 rounded-lg flex items-center justify-center ${iconVariants[variant]}`}>
					<Icon className="h-3.5 w-3.5" />
				</div>
			</div>

			<div className={`text-2xl font-semibold ${valueVariants[variant]}`}>{value}</div>
			{description && <p className="text-xs text-muted-foreground mt-0.5">{description}</p>}

			{sparklineColor && (
				<div className="mt-2">
					<Suspense fallback={<div className="w-full mt-3" style={{ height: 48 }} />}>
						<Sparkline color={sparklineColor} type={sparklineType} data={sparklineData ?? []} />
					</Suspense>
				</div>
			)}
		</div>
	);
}
