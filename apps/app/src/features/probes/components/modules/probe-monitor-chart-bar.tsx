import { useTranslation } from 'react-i18next';
import { HoverCard, HoverCardContent, HoverCardTrigger } from '@/components/atoms/hover-card';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';
import { getStatusTokens } from '@/lib/status.ts';
import { formatDayLong, formatTime } from '@/lib/datetime.ts';

type BarItem = Monitor | { type: 'pause'; id: string; timestamp: Date };

interface ProbeMonitorChartBarProps {
	monitors: Monitor[];
	probeStatus: string;
	barCount?: number;
}

export default function ProbeMonitorChartBar({ monitors, probeStatus, barCount = 40 }: ProbeMonitorChartBarProps) {
	const { t, i18n } = useTranslation();

	/* Les secondes comptent ici : deux checks consecutifs peuvent tomber dans la meme minute. */
	const formatBarTime = (value: string) => formatTime(value, i18n.language, { withSeconds: true });
	const formatBarDate = (value: string) => formatDayLong(value, i18n.language);

	const isPaused = probeStatus === 'PAUSE';

	const buildBarsWithGaps = (): BarItem[] => {
		const allBars: BarItem[] = [];
		const sortedMonitors = [...monitors].sort((a, b) => new Date(a.run_at).getTime() - new Date(b.run_at).getTime());

		for (let i = 0; i < sortedMonitors.length; i++) {
			const currentLog = sortedMonitors[i];

			if (i > 0) {
				const previousTime = new Date(sortedMonitors[i - 1].run_at).getTime();
				const currentTime = new Date(currentLog.run_at).getTime();
				const minutesDiff = Math.floor((currentTime - previousTime) / (60 * 1000));

				for (let j = 1; j < minutesDiff; j++) {
					allBars.push({
						type: 'pause',
						id: `pause-${i}-${j}`,
						timestamp: new Date(previousTime + j * 60 * 1000),
					});
				}
			}

			allBars.push(currentLog);
		}

		if (isPaused && sortedMonitors.length > 0) {
			const lastLogTime = new Date(sortedMonitors[sortedMonitors.length - 1].run_at).getTime();
			const minutesSinceLastLog = Math.floor((Date.now() - lastLogTime) / (60 * 1000));

			for (let j = 1; j < minutesSinceLastLog; j++) {
				allBars.push({
					type: 'pause',
					id: `pause-current-${j}`,
					timestamp: new Date(lastLogTime + j * 60 * 1000),
				});
			}
		}

		return allBars;
	};

	const allBars = buildBarsWithGaps();
	const bars = allBars.slice(-barCount);
	const emptyBars = barCount - bars.length;

	const barBase = 'h-8 flex-1 rounded-[2px] transition-opacity hover:opacity-60';

	return (
		<div className="w-full">
			<div className="my-3 flex h-8 w-full items-end gap-[3px]">
				{/*
				 * Slots with no check at all. Much lighter than the paused colour: "we never
				 * looked" and "we deliberately stopped looking" are different facts.
				 */}
				{Array.from({ length: emptyBars }).map((_, i) => (
					<div key={`empty-${i}`} className="bg-status-nodata h-8 flex-1 rounded-[2px]" />
				))}

				{bars.map((item) => {
					if ('type' in item && item.type === 'pause') {
						return (
							<HoverCard key={item.id} openDelay={100}>
								<HoverCardTrigger asChild>
									<div className={`bg-status-paused cursor-pointer ${barBase}`} />
								</HoverCardTrigger>
								<HoverCardContent className="w-80">
									<BarTooltip
										title={t('status.paused')}
										message={t('monitors.description.paused_slot')}
										date={formatBarDate(item.timestamp.toISOString())}
										time={formatBarTime(item.timestamp.toISOString())}
									/>
								</HoverCardContent>
							</HoverCard>
						);
					}

					const check = item as Monitor;
					const tokens = getStatusTokens(check.status);

					return (
						<HoverCard key={check.id} openDelay={100}>
							<HoverCardTrigger asChild>
								<div className={`${tokens.solid} cursor-pointer ${barBase}`} />
							</HoverCardTrigger>
							<HoverCardContent className="w-80">
								<BarTooltip
									title={t(tokens.labelKey)}
									message={check.message}
									date={formatBarDate(check.run_at.toString())}
									time={formatBarTime(check.run_at.toString())}
									responseTime={check.status === 'SUCCESS' ? check.response_time : undefined}
									tone={tokens.fg}
								/>
							</HoverCardContent>
						</HoverCard>
					);
				})}
			</div>
		</div>
	);
}

interface BarTooltipProps {
	title: string;
	message: string;
	date: string;
	time: string;
	responseTime?: number;
	tone?: string;
}

function BarTooltip({ title, message, date, time, responseTime, tone }: BarTooltipProps) {
	return (
		<div className="space-y-2">
			<h4 className={`text-sm font-semibold ${tone ?? ''}`}>{title}</h4>
			<p className="text-sm break-words">{message}</p>

			{responseTime !== undefined && (
				<p className="text-muted-foreground text-xs">
					<span className="tabular text-foreground font-medium">{responseTime} ms</span>
				</p>
			)}

			<div className="border-border border-t pt-2">
				<div className="text-muted-foreground tabular text-xs">{date}</div>
				<div className="text-muted-foreground tabular text-xs">{time}</div>
			</div>
		</div>
	);
}
