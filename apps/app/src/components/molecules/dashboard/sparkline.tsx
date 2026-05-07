import { Area, AreaChart, Bar, BarChart, XAxis, YAxis } from 'recharts';
import { type ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent } from '@/components/atoms/chart.tsx';

interface SparklineProps {
	color: string;
	type?: 'line' | 'bar';
	data: { bucket?: Date; hour?: Date; value: number }[];
}

export default function Sparkline({ color, type = 'line', data = [] }: SparklineProps) {
	const chartConfig = {
		value: {
			color,
		},
	} satisfies ChartConfig;

	if (type === 'bar') {
		const filled = Array.from({ length: 24 }, (_, h) => {
			const found = data.find((d) => new Date((d.hour ?? d.bucket)!).getUTCHours() === h);
			return { hour: h, value: found?.value ?? 0 };
		});

		return (
			<ChartContainer config={chartConfig} className="w-full mt-3" style={{ height: 48 }}>
				<BarChart data={filled} margin={{ top: 0, right: 0, bottom: 0, left: 0 }} barCategoryGap="20%">
					<XAxis dataKey="hour" hide />
					<YAxis hide domain={['auto', 'auto']} />
					<ChartTooltip content={<ChartTooltipContent hideLabel formatter={(value) => [`${value}`, 'Incidents']} />} />
					<Bar dataKey="value" fill="var(--color-value)" radius={[2, 2, 0, 0]} />
				</BarChart>
			</ChartContainer>
		);
	}

	const chartData = data.map((d, i) => ({
		index: i,
		value: d.value,
	}));

	if (chartData.length === 0) {
		return <div className="w-full mt-3" style={{ height: 48 }} />;
	}

	const gradId = `grad-${color.replace(/[^a-zA-Z0-9]/g, '')}`;

	return (
		<ChartContainer config={chartConfig} className="w-full mt-3" style={{ height: 48 }}>
			<AreaChart data={chartData} margin={{ top: 2, right: 0, bottom: 0, left: 0 }}>
				<defs>
					<linearGradient id={gradId} x1="0" y1="0" x2="0" y2="1">
						<stop offset="0%" stopColor="var(--color-value)" stopOpacity={0.25} />
						<stop offset="100%" stopColor="var(--color-value)" stopOpacity={0} />
					</linearGradient>
				</defs>
				<XAxis dataKey="index" hide />
				<YAxis hide domain={['auto', 'auto']} />
				<ChartTooltip content={<ChartTooltipContent hideLabel formatter={(value) => [`${value}`, '']} />} />
				<Area
					type="monotone"
					dataKey="value"
					stroke="var(--color-value)"
					strokeWidth={1.5}
					fill={`url(#${gradId})`}
					dot={false}
					activeDot={{ r: 3, fill: 'var(--color-value)', strokeWidth: 0 }}
					isAnimationActive
					animationDuration={400}
					animationEasing="ease-in-out"
				/>
			</AreaChart>
		</ChartContainer>
	);
}
