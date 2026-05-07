interface UptimesProps {
	h24: number;
	d7: number;
	d30: number;
}

const uptimeItems = [
	{ label: 'Uptime 24h', key: 'h24' },
	{ label: 'Uptime 7j', key: 'd7' },
	{ label: 'Uptime 30j', key: 'd30' },
] as const;

export default function ProbeUptime({ uptimes }: { uptimes: UptimesProps }) {
	function getUptimeColor(value: number) {
		if (value >= 99.9) return 'text-green-600';
		if (value >= 99) return 'text-yellow-500';
		return 'text-red-500';
	}

	return (
		<div className="grid grid-cols-3 gap-2 mt-4">
			{uptimeItems.map(({ label, key }) => (
				<div key={key} className="bg-gray-50 border border-gray-100 rounded-lg py-3 px-4">
					<h3 className="text-xs uppercase tracking-wide text-gray-400 mb-1">{label}</h3>
					<p className={`text-2xl font-medium ${getUptimeColor(uptimes[key])}`}>{uptimes[key].toFixed(1)}%</p>
				</div>
			))}
		</div>
	);
}
