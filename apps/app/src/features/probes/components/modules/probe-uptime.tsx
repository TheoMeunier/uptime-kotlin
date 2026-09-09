import { useTranslation } from 'react-i18next';
import Metric, { MetricRow } from '@/components/molecules/metric.tsx';
import { uptimeState } from '@/lib/status.ts';

interface UptimesProps {
	h24: number;
	d7: number;
	d30: number;
}

const uptimeItems = [
	{ labelKey: 'monitors.uptime.h24', key: 'h24' },
	{ labelKey: 'monitors.uptime.d7', key: 'd7' },
	{ labelKey: 'monitors.uptime.d30', key: 'd30' },
] as const;

export default function ProbeUptime({ uptimes }: { uptimes: UptimesProps }) {
	const { t } = useTranslation();

	return (
		<div className="mt-5">
			<MetricRow>
				{uptimeItems.map(({ labelKey, key }) => (
					<Metric
						key={key}
						variant="inline"
						label={t(labelKey)}
						value={`${uptimes[key].toFixed(1)}%`}
						state={uptimeState(uptimes[key])}
					/>
				))}
			</MetricRow>
		</div>
	);
}
