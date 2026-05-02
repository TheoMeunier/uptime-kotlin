interface SparkLineProps {
	color: string;
	type?: 'line' | 'bar';
	data: { bucket?: Date; hour?: Date; value: number }[];
}

export default function Sparkline({ color, type = 'line', data = [] }: SparkLineProps) {
	if (type === 'bar') {
		const filled = Array.from({ length: 24 }, (_, h) => {
			const found = data.find((d) => {
				const date = new Date((d.hour ?? d.bucket)!);
				return date.getUTCHours() === h;
			});
			return { hour: h, value: found?.value ?? 0 };
		});

		const max = Math.max(...filled.map((d) => d.value), 1);
		const barWidth = 6;
		const gap = (200 - barWidth) / 23;

		return (
			<svg className="w-full h-9 mt-3" viewBox="0 0 200 36" preserveAspectRatio="none">
				{filled.map((d, i) => {
					const barHeight = Math.max(d.value > 0 ? 2 : 0, (d.value / max) * 34);
					return (
						<rect
							key={i}
							x={i * gap}
							y={36 - barHeight}
							width={barWidth}
							height={barHeight}
							fill={color}
							rx="1"
							fillOpacity={d.value > 0 ? 0.8 : 0.15}
						/>
					);
				})}
			</svg>
		);
	}

	if (data.length === 0) {
		return (
			<svg className="w-full h-9 mt-3" viewBox="0 0 200 36" preserveAspectRatio="none">
				<polyline
					points="0,28 20,24 40,26 60,18 80,22 100,14 120,20 140,10 160,16 180,8 200,12"
					fill="none"
					stroke={color}
					strokeWidth="1.5"
				/>
			</svg>
		);
	}

	const values = data.map((d) => d.value);
	const min = Math.min(...values);
	const max = Math.max(...values);
	const range = max - min || 1;

	const points = data
		.map((d, i) => {
			const x = (i / Math.max(data.length - 1, 1)) * 200;
			const y = 32 - ((d.value - min) / range) * 28 + 4;
			return `${x},${y}`;
		})
		.join(' ');

	const areaPoints = points + ` 200,36 0,36`;

	return (
		<svg className="w-full h-9 mt-3" viewBox="0 0 200 36" preserveAspectRatio="none">
			<defs>
				<linearGradient id={`grad-${color.replace('#', '')}`} x1="0" y1="0" x2="0" y2="1">
					<stop offset="0%" stopColor={color} stopOpacity="0.15" />
					<stop offset="100%" stopColor={color} stopOpacity="0" />
				</linearGradient>
			</defs>
			<polyline points={points} fill="none" stroke={color} strokeWidth="1.5" strokeLinejoin="round" />
			<polyline points={areaPoints} fill={`url(#grad-${color.replace('#', '')})`} stroke="none" />
		</svg>
	);
}
