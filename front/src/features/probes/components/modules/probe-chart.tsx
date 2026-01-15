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
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';

export default function ProbeChart({
	monitors,
	lastHour,
	setLastHour,
}: {
	monitors: Monitor[];
	lastHour: number;
	setLastHour: (value: number) => void;
}) {
	const { t } = useTranslation();

	const chartConfig = {
		response_time: {
			label: 'Response Time (ms)',
			color: 'green',
		},
	} satisfies ChartConfig;

	const chartData = monitors.map((monitor) => ({
		date: new Date(monitor.run_at).toISOString(),
		response_time: monitor.response_time,
	}));

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
				<ChartContainer config={chartConfig} className="aspect-auto h-[250px] w-full">
					<AreaChart data={chartData}>
						<CartesianGrid vertical={false} strokeDasharray="3 3" />
						<XAxis
							dataKey="date"
							tickLine={false}
							axisLine={false}
							tickMargin={8}
							minTickGap={32}
							tickFormatter={(value) =>
								new Date(value).toLocaleTimeString('en-US', {
									hour: '2-digit',
									minute: '2-digit',
								})
							}
						/>
						<YAxis tickLine={false} axisLine={true} tickMargin={8} />
						<ChartTooltip
							cursor={false}
							content={
								<ChartTooltipContent
									labelFormatter={(value) =>
										new Date(value).toLocaleTimeString('en-US', {
											hour: '2-digit',
											minute: '2-digit',
										})
									}
									indicator="dot"
								/>
							}
						/>
						<Area dataKey="response_time" type="natural" fill="#dcfce7" stroke="#22c55e" connectNulls={false} />
						<ChartLegend content={<ChartLegendContent />} />
					</AreaChart>
				</ChartContainer>
			</CardContent>
		</Card>
	);
}
