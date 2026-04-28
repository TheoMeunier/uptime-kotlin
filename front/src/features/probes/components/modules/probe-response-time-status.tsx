import { useMemo } from 'react';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';

const statsItems = [
	{ label: 'Current', key: 'current' as const },
	{ label: 'Average', key: 'average' as const },
	{ label: 'Max Peak', key: 'max' as const },
	{ label: 'Min', key: 'min' as const },
];

function getStatColor(key: string, value: number) {
	if (key === 'max' && value >= 300) return 'text-yellow-600';
	if (key === 'min') return 'text-green-600';
	return 'text-gray-800';
}

export default function ResponseTimeStats({ monitors }: { monitors: Monitor[] }) {
	const stats = useMemo(() => {
		const times = monitors.map((m) => m.response_time);
		return {
			current: times.at(-1) ?? 0,
			average: Math.round(times.reduce((a, b) => a + b, 0) / times.length),
			max: Math.max(...times),
			min: Math.min(...times),
		};
	}, [monitors]);

	return (
		<div className="grid grid-cols-4 gap-2 mt-4">
			{statsItems.map(({ label, key }) => (
				<div key={key} className="bg-gray-50 border border-gray-100 rounded-lg py-3 px-4">
					<h3 className="text-xs uppercase tracking-wide text-gray-400 mb-1">{label}</h3>
					<p className={`text-2xl font-medium ${getStatColor(key, stats[key])}`}>
						{stats[key]} <span className="text-sm font-normal text-gray-400">ms</span>
					</p>
				</div>
			))}
		</div>
	);
}
