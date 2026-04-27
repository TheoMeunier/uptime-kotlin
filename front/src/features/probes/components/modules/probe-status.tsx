import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';

interface StatusConfig {
	ping: string;
	dot: string;
	badge: string;
	label: string;
}

const statusConfig: Record<ProbeStatusEnum, StatusConfig> = {
	[ProbeStatusEnum.SUCCESS]: {
		ping: 'bg-green-400',
		dot: 'bg-green-500',
		badge: 'bg-green-100 text-green-700',
		label: 'Healthy',
	},
	[ProbeStatusEnum.WARNING]: {
		ping: 'bg-orange-400',
		dot: 'bg-orange-500',
		badge: 'bg-orange-100 text-orange-700',
		label: 'Warning',
	},
	[ProbeStatusEnum.FAILURE]: {
		ping: 'bg-red-400',
		dot: 'bg-red-500',
		badge: 'bg-red-100 text-red-700',
		label: 'Unhealthy',
	},
	[ProbeStatusEnum.PAUSE]: {
		ping: 'bg-gray-400',
		dot: 'bg-gray-500',
		badge: 'bg-gray-100 text-gray-700',
		label: 'Paused',
	},
};

const defaultConfig: StatusConfig = {
	ping: 'bg-red-400',
	dot: 'bg-red-500',
	badge: 'bg-red-100 text-red-700',
	label: 'Unhealthy',
};

interface ProbeStatusProps {
	status: ProbeStatusEnum;
	size?: string;
	showLabel?: boolean;
}

export default function ProbeStatus({ status, size = 'size-2.5', showLabel = true }: ProbeStatusProps) {
	const config: StatusConfig = statusConfig[status] ?? defaultConfig;

	return (
		<div className={`inline-flex items-center gap-2 rounded-full px-3 py-1.5 text-sm font-medium ${config.badge}`}>
			<span className={`relative flex ${size}`}>
				<span className={`absolute inline-flex h-full w-full animate-ping rounded-full ${config.ping} opacity-75`} />
				<span className={`relative inline-flex ${size} rounded-full ${config.dot}`} />
			</span>
			{showLabel && config.label}
		</div>
	);
}
