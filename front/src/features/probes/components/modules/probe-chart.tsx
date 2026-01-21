import {
	type ChartConfig,
	ChartContainer,
	ChartLegend,
	ChartLegendContent,
	ChartTooltip,
	ChartTooltipContent,
} from '@/components/atoms/chart.tsx';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/atoms/select.tsx';
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from 'recharts';
import { useTranslation } from 'react-i18next';
import { useMemo } from 'react';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';
import type ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';

export default function ProbeChart({
	monitors,
	lastHour,
	setLastHour,
	monitorStatus,
}: {
	monitors: Monitor[];
	lastHour: number;
	setLastHour: (value: number) => void;
	monitorStatus: ProbeStatusEnum;
}) {
	const { t } = useTranslation();

	console.log(monitors);

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

	const getTimeRangeLabel = () => {
		switch (lastHour) {
			case 1:
				return t('timeRanger.last_1_hour');
			case 3:
				return t('timeRanger.last_3_hours');
			case 6:
				return t('timeRanger.last_6_hours');
			case 24:
				return t('timeRanger.last_24_hours');
			case 168:
				return t('timeRanger.last_7_days');
			default:
				return '';
		}
	};

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
		<Card className="pt-0">
			<CardHeader className="flex items-center gap-2 space-y-0 border-b py-5 sm:flex-row">
				<div className="grid flex-1 gap-1">
					<CardTitle>Probe Response Time</CardTitle>
					<CardDescription>Showing probe response times for {getTimeRangeLabel()}</CardDescription>
				</div>
				<Select value={lastHour.toString()} onValueChange={(value) => setLastHour(Number(value))}>
					<SelectTrigger className="hidden w-[160px] rounded-lg sm:ml-auto sm:flex" aria-label="Select a value">
						<SelectValue placeholder="Select time range" />
					</SelectTrigger>
					<SelectContent className="rounded-xl">
						<SelectItem value="1" className="rounded-lg">
							{t('timeRanger.last_1_hour')}
						</SelectItem>
						<SelectItem value="3" className="rounded-lg">
							{t('timeRanger.last_3_hours')}
						</SelectItem>
						<SelectItem value="6" className="rounded-lg">
							{t('timeRanger.last_6_hours')}
						</SelectItem>
						<SelectItem value="24" className="rounded-lg">
							{t('timeRanger.last_24_hours')}
						</SelectItem>
						<SelectItem value="168" className="rounded-lg">
							{t('timeRanger.last_7_days')}
						</SelectItem>
					</SelectContent>
				</Select>
			</CardHeader>
			<CardContent className="px-2 pt-4 sm:px-6 sm:pt-6">
				{!hasData ? (
					<div className="flex h-[250px] w-full items-center justify-center text-muted-foreground">
						No data available for the selected time range
					</div>
				) : (
					<ChartContainer config={chartConfig} className="aspect-auto h-[250px] w-full">
						<AreaChart data={chartData}>
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
								fill={monitorStatus === 'FAILURE' ? '#fee2e2' : '#dcfce7'} // rouge clair / vert clair
								stroke={monitorStatus === 'FAILURE' ? '#ef4444' : '#22c55e'}
								connectNulls={false}
							/>
							<ChartLegend content={<ChartLegendContent />} />
						</AreaChart>
					</ChartContainer>
				)}
			</CardContent>
		</Card>
	);
}
