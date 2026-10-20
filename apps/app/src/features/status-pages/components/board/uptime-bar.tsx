import { HoverCard, HoverCardContent, HoverCardTrigger } from '@/components/atoms/hover-card';
import { cn } from '@/lib/utils.ts';
import UptimeBucketTooltip from '@/features/status-pages/components/board/uptime-bucket-tooltip.tsx';
import {
	BUCKET_TONE,
	bucketState,
	UPTIME_PERIODS,
	type UptimePeriod,
} from '@/features/status-pages/lib/uptime-period.ts';
import type { ProbeUptimeBar } from '@/features/status-pages/schemas/status-uptime.schema.ts';

interface UptimeBarProps {
	period: UptimePeriod;
	bar?: ProbeUptimeBar;
	compact?: boolean;
}

export default function UptimeBar({ period, bar, compact = false }: UptimeBarProps) {
	const spec = UPTIME_PERIODS[period];
	const daily = spec.unit === 'days' && period !== 'D7';
	const height = compact ? 'h-6' : 'h-8';
	const gap = spec.bars > 60 ? 'gap-px' : compact ? 'gap-[2px]' : 'gap-[3px]';

	return (
		<div className={cn('flex w-full items-end', height, gap, !compact && 'my-3')}>
			{bar
				? bar.buckets.map((bucket) => (
						<HoverCard key={bucket.starts_at} openDelay={100}>
							<HoverCardTrigger asChild>
								<div
									className={cn(
										height,
										'min-w-px flex-1 cursor-pointer rounded-[2px] transition-opacity hover:opacity-60',
										BUCKET_TONE[bucketState(bucket)].solid
									)}
								/>
							</HoverCardTrigger>
							<HoverCardContent className="w-64">
								<UptimeBucketTooltip bucket={bucket} daily={daily} />
							</HoverCardContent>
						</HoverCard>
					))
				: Array.from({ length: spec.bars }).map((_, index) => (
						<div key={index} className={cn(height, 'bg-status-nodata min-w-px flex-1 animate-pulse rounded-[2px]')} />
					))}
		</div>
	);
}
