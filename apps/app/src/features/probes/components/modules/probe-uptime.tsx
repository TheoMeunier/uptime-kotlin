import { useTranslation } from 'react-i18next';
import Metric from '@/components/molecules/metric.tsx';
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
		<div className="mt-4 grid grid-cols-3 gap-2">
			{uptimeItems.map(({ labelKey, key }) => (
				<Metric key={key} label={t(labelKey)} value={`${uptimes[key].toFixed(1)}%`} state={uptimeState(uptimes[key])} />
			))}
		</div>
	);
}
