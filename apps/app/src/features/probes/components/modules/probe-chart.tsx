import { type ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent } from '@/components/atoms/chart.tsx';
import { Area, AreaChart, CartesianGrid, ReferenceArea, ReferenceDot, ReferenceLine, XAxis, YAxis } from 'recharts';
import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';
import type ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import type { MaintenanceOccurrence } from '@/features/maintenances/schemas/maintenance.schema.ts';
import { formatDayShort, formatTime } from '@/lib/datetime.ts';

export default function ProbeChart({
	monitors,
	lastHour,
	monitorStatus,
	maintenancePeriods = [],
}: {
	monitors: Monitor[];
	lastHour: number;
	monitorStatus: ProbeStatusEnum;
	maintenancePeriods?: MaintenanceOccurrence[];
}) {
	const { t, i18n } = useTranslation();

	const isFailing = monitorStatus === 'FAILURE';
	const seriesColor = isFailing ? 'var(--status-down)' : 'var(--status-up)';
	const seriesFill = isFailing ? 'var(--status-down-bg)' : 'var(--status-up-bg)';

	const chartConfig = {
		response_time: {
			label: t('monitors.chart.response_time'),
			color: seriesColor,
		},
	} satisfies ChartConfig;

	const { chartData, xAxisDomain } = useMemo(() => {
		const now = Date.now();
		const startTime = now - lastHour * 60 * 60 * 1000;

		const gapThreshold = lastHour <= 6 ? 10 * 60 * 1000 : 60 * 60 * 1000;

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
					dataWithGaps.push({ timestamp: filteredMonitors[i].timestamp + 1, response_time: null });
				}
			}
		}

		return { chartData: dataWithGaps, xAxisDomain: [startTime, now] };
	}, [monitors, lastHour]);

	const { average, lastPoint } = useMemo(() => {
		const values = chartData.filter((d) => d.response_time !== null) as {
			timestamp: number;
			response_time: number;
		}[];

		if (values.length === 0) return { average: null, lastPoint: null };

		const sum = values.reduce((acc, d) => acc + d.response_time, 0);
		return {
			average: Math.round(sum / values.length),
			lastPoint: values[values.length - 1],
		};
	}, [chartData]);

	const formatTick = (timestamp: number) =>
		lastHour === 168 ? formatDayShort(timestamp, i18n.language) : formatTime(timestamp, i18n.language);

	const maintenanceBands = useMemo(() => {
		const [start, end] = xAxisDomain;

		return maintenancePeriods
			.map((period) => ({
				id: period.id,
				from: Math.max(new Date(period.starts_at).getTime(), start),
				to: Math.min(new Date(period.ends_at).getTime(), end),
			}))
			.filter((band) => Number.isFinite(band.from) && Number.isFinite(band.to) && band.to > band.from);
	}, [maintenancePeriods, xAxisDomain]);

	const hasData = chartData.some((d) => d.response_time !== null);

	if (!hasData) {
		return (
			<div className="text-muted-foreground flex h-[250px] w-full items-center justify-center">
				{t('monitors.chart.empty')}
			</div>
		);
	}

	return (
		<div className="relative">
			<ChartContainer config={chartConfig} className="h-[250px] w-full">
				<AreaChart data={chartData} margin={{ top: 8, right: 12, left: 4, bottom: 5 }}>
					<CartesianGrid vertical={false} strokeDasharray="3 3" />
					<XAxis
						dataKey="timestamp"
						type="number"
						domain={xAxisDomain}
						tickLine={false}
						axisLine={false}
						tickMargin={8}
						tickFormatter={formatTick}
					/>
					<YAxis
						tickLine={false}
						axisLine={false}
						tickMargin={8}
						width={68}
						tickFormatter={(value: number) => `${value} ms`}
					/>
					<ChartTooltip
						cursor={false}
						content={
							<ChartTooltipContent
								labelFormatter={(_, payload) => {
									const timestamp = payload?.[0]?.payload?.timestamp;
									return timestamp ? formatTick(timestamp) : '';
								}}
								indicator="dot"
							/>
						}
					/>

					{maintenanceBands.map((band) => (
						<ReferenceArea
							key={band.id}
							x1={band.from}
							x2={band.to}
							fill="var(--status-maintenance)"
							fillOpacity={0.12}
							stroke="var(--status-maintenance)"
							strokeOpacity={0.25}
							ifOverflow="hidden"
						/>
					))}

					<Area
						dataKey="response_time"
						type="monotone"
						fill={seriesFill}
						stroke={seriesColor}
						connectNulls={false}
						isAnimationActive={false}
					/>

					{average !== null && (
						<ReferenceLine
							y={average}
							stroke="var(--muted-foreground)"
							strokeDasharray="4 4"
							strokeOpacity={0.7}
							label={{
								value: t('monitors.chart.average_reference', { value: average }),
								position: 'insideTopRight',
								fill: 'var(--muted-foreground)',
								fontSize: 11,
							}}
						/>
					)}

					{lastPoint && (
						<ReferenceDot
							x={lastPoint.timestamp}
							y={lastPoint.response_time}
							r={4}
							fill={seriesColor}
							stroke="var(--background)"
							strokeWidth={2}
							isFront
						/>
					)}
				</AreaChart>
			</ChartContainer>
		</div>
	);
}
