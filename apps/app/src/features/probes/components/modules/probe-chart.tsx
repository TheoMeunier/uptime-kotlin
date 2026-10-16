import { type ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent } from '@/components/atoms/chart.tsx';
import {
	Area,
	AreaChart,
	CartesianGrid,
	Customized,
	ReferenceArea,
	ReferenceDot,
	ReferenceLine,
	XAxis,
	YAxis,
} from 'recharts';
import { useId, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';
import type { MaintenanceOccurrence } from '@/features/maintenances/schemas/maintenance.schema.ts';
import type { IncidentSpan } from '@/features/incidents/schemas/incident.schema.ts';
import { formatDayShort, formatTime } from '@/lib/datetime.ts';

const seriesColor = 'var(--primary)';
const incidentColor = 'var(--status-down)';
const MIN_BAND_RATIO = 0.004;

interface Band {
	id: string;
	from: number;
	to: number;
}

function IncidentGradient({
	id,
	domain: [start, end],
	bands,
	offset,
}: {
	id: string;
	domain: number[];
	bands: Band[];
	offset?: { left: number; width: number };
}) {
	if (!offset) return null;

	const at = (time: number) => (time - start) / (end - start);

	return (
		<defs>
			<linearGradient
				id={id}
				gradientUnits="userSpaceOnUse"
				x1={offset.left}
				y1={0}
				x2={offset.left + offset.width}
				y2={0}
			>
				<stop offset={0} style={{ stopColor: seriesColor }} />
				{bands.flatMap((band) => [
					<stop key={`${band.id}-in`} offset={at(band.from)} style={{ stopColor: seriesColor }} />,
					<stop key={`${band.id}-red-in`} offset={at(band.from)} style={{ stopColor: incidentColor }} />,
					<stop key={`${band.id}-red-out`} offset={at(band.to)} style={{ stopColor: incidentColor }} />,
					<stop key={`${band.id}-out`} offset={at(band.to)} style={{ stopColor: seriesColor }} />,
				])}
				<stop offset={1} style={{ stopColor: seriesColor }} />
			</linearGradient>
		</defs>
	);
}

export default function ProbeChart({
	monitors,
	lastHour,
	maintenancePeriods = [],
	incidentPeriods = [],
}: {
	monitors: Monitor[];
	lastHour: number;
	maintenancePeriods?: MaintenanceOccurrence[];
	incidentPeriods?: IncidentSpan[];
}) {
	const { t, i18n } = useTranslation();
	const gradientId = `incident-gradient-${useId().replace(/[^a-zA-Z0-9_-]/g, '')}`;

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

	const incidentBands = useMemo(() => {
		const [start, end] = xAxisDomain;
		const minWidth = (end - start) * MIN_BAND_RATIO;

		return incidentPeriods
			.flatMap((period) => {
				const startedAt = period.started_at.getTime();
				const endedAt = period.ended_at?.getTime() ?? end;
				if (startedAt >= end || endedAt <= start) return [];

				const from = Math.min(Math.max(startedAt, start), end - minWidth);
				const to = Math.min(Math.max(endedAt, from + minWidth), end);

				return [{ id: period.id, from, to }];
			})
			.sort((a, b) => a.from - b.from);
	}, [incidentPeriods, xAxisDomain]);

	const isDuringIncident = (timestamp: number | undefined) =>
		timestamp !== undefined && incidentBands.some((band) => timestamp >= band.from && timestamp <= band.to);
	const seriesPaint = incidentBands.length > 0 ? `url(#${gradientId})` : seriesColor;

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
						content={({ active, payload, label }) => (
							<ChartTooltipContent
								active={active}
								payload={payload}
								label={label}
								color={isDuringIncident(payload?.[0]?.payload?.timestamp) ? incidentColor : seriesColor}
								labelFormatter={(_, payload) => {
									const timestamp = payload?.[0]?.payload?.timestamp;
									return timestamp ? formatTick(timestamp) : '';
								}}
								indicator="dot"
							/>
						)}
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

					{incidentBands.map((band) => (
						<ReferenceArea
							key={band.id}
							x1={band.from}
							x2={band.to}
							fill={incidentColor}
							fillOpacity={0.06}
							stroke="none"
							ifOverflow="hidden"
						/>
					))}

					{incidentBands.length > 0 && (
						<Customized component={<IncidentGradient id={gradientId} domain={xAxisDomain} bands={incidentBands} />} />
					)}

					<Area
						dataKey="response_time"
						type="monotone"
						fill={seriesPaint}
						fillOpacity={0.12}
						stroke={seriesPaint}
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
							fill={isDuringIncident(lastPoint.timestamp) ? incidentColor : seriesColor}
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
