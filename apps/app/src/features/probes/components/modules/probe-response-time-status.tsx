import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import Metric from '@/components/molecules/metric.tsx';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';

const statsItems = [
	{ labelKey: 'monitors.latency.current', key: 'current' as const },
	{ labelKey: 'monitors.latency.average', key: 'average' as const },
	{ labelKey: 'monitors.latency.max_peak', key: 'max' as const },
	{ labelKey: 'monitors.latency.min', key: 'min' as const },
];

export default function ResponseTimeStats({ monitors }: { monitors: Monitor[] }) {
	const { t } = useTranslation();

	const stats = useMemo(() => {
		const times = monitors.map((m) => m.response_time);
		if (times.length === 0) return { current: 0, average: 0, max: 0, min: 0 };

		return {
			current: times.at(-1) ?? 0,
			average: Math.round(times.reduce((a, b) => a + b, 0) / times.length),
			max: Math.max(...times),
			min: Math.min(...times),
		};
	}, [monitors]);

	return (
		<div className="mt-4 grid grid-cols-2 gap-2 sm:grid-cols-4">
			{statsItems.map(({ labelKey, key }) => (
				<Metric key={key} label={t(labelKey)} value={stats[key]} unit="ms" />
			))}
		</div>
	);
}
