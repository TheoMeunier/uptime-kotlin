import {
	type ChartConfig,
	ChartContainer,
	ChartLegend,
	ChartLegendContent,
	ChartTooltip,
	ChartTooltipContent,
} from '@/components/atoms/chart.tsx';
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from 'recharts';
import { useMemo } from 'react';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';
import type ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';

export default function ProbeChart({
	monitors,
	lastHour,
	monitorStatus,
}: {
	monitors: Monitor[];
	lastHour: number;
	monitorStatus: ProbeStatusEnum;
}) {
	const chartConfig = {
		response_time: {
			label: 'Response Time (ms)',
			color: 'green',
		},
	} satisfies ChartConfig;

	const chartData = useMemo(() => {
		const now = Date.now();
		const startTime = now - lastHour * 60 * 60 * 1000;

		const gapThreshold = lastHour <= 6 ? 10 * 60 * 1000 : 60 * 60 * 1000; // 10 min pour <= 6h, 1h pour le reste

		const filteredMonitors = monitors
			.filter((monitor) => {
				const monitorTime = new Date(monitor.run_at).getTime();
				return monitorTime >= startTime && monitorTime <= now && monitor.response_time != null;
			})
			.map((monitor) => ({
				timestamp: new Date(monitor.run_at).getTime(),
				response_time: monitor.response_time,
			}))
			.sort((a, b) => a.timestamp - b.timestamp);

		const dataWithGaps: Array<{ timestamp: number; response_time: number | null }> = [];

		for (let i = 0; i < filteredMonitors.length; i++) {
			dataWithGaps.push(filteredMonitors[i]);

			if (i < filteredMonitors.length - 1) {
				const gap = filteredMonitors[i + 1].timestamp - filteredMonitors[i].timestamp;

				if (gap > gapThreshold) {
					dataWithGaps.push({
						timestamp: filteredMonitors[i].timestamp + 1,
						response_time: null,
					});
				}
			}
		}

		return dataWithGaps;
	}, [monitors, lastHour]);

	const formatTime = (timestamp: number) => {
		const date = new Date(timestamp);

		if (lastHour === 168) {
			return date.toLocaleDateString('en-US', {
				month: 'short',
				day: 'numeric',
			});
		}

		return date.toLocaleTimeString('en-US', {
			hour: '2-digit',
			minute: '2-digit',
		});
	};

	const xAxisDomain = useMemo(() => {
		const now = Date.now();
		const startTime = now - lastHour * 60 * 60 * 1000;
		return [startTime, now];
	}, [lastHour]);

	const hasData = chartData.some((d) => d.response_time !== null);

	return (
		<div className="relative">
			{!hasData ? (
				<div className="flex h-[250px] w-full items-center justify-center text-muted-foreground">
					No data available for the selected time range
				</div>
			) : (
				<ChartContainer config={chartConfig} className="h-[250px] w-full">
					<AreaChart data={chartData} margin={{ top: 5, right: 5, left: -20, bottom: 5 }}>
						<CartesianGrid vertical={false} strokeDasharray="3 3" />
						<XAxis
							dataKey="timestamp"
							type="number"
							domain={xAxisDomain}
							tickLine={false}
							axisLine={false}
							tickMargin={8}
							minTickGap={32}
							tickFormatter={formatTime}
						/>
						<YAxis tickLine={false} axisLine={true} tickMargin={8} />
						<ChartTooltip
							cursor={false}
							content={
								<ChartTooltipContent
									labelFormatter={(_, payload) => {
										const timestamp = payload?.[0]?.payload?.timestamp;
										return timestamp ? formatTime(timestamp) : '';
									}}
									indicator="dot"
								/>
							}
						/>
						<Area
							dataKey="response_time"
							type="monotone"
							fill={monitorStatus === 'FAILURE' ? '#fee2e2' : '#dcfce7'}
							stroke={monitorStatus === 'FAILURE' ? '#ef4444' : '#22c55e'}
							connectNulls={false}
						/>
						<ChartLegend content={<ChartLegendContent />} />
					</AreaChart>
				</ChartContainer>
			)}
		</div>
	);
}
