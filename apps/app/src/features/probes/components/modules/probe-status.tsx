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

const sizeConfig = {
	sm: {
		container: 'px-2 py-1 text-xs gap-1.5',
		dot: 'h-2 w-2',
	},
	md: {
		container: 'px-3 py-1.5 text-sm gap-2',
		dot: 'h-2.5 w-2.5',
	},
	lg: {
		container: 'px-4 py-2 text-base gap-2.5',
		dot: 'h-3 w-3',
	},
};

type SizeVariant = keyof typeof sizeConfig;

interface ProbeStatusProps {
	status: ProbeStatusEnum;
	size?: SizeVariant;
	showLabel?: boolean;
}

export default function ProbeStatus({ status, size = 'md', showLabel = true }: ProbeStatusProps) {
	const config = statusConfig[status] ?? defaultConfig;
	const sizeStyle = sizeConfig[size];

	return (
		<div className={`inline-flex items-center rounded-full font-medium ${sizeStyle.container} ${config.badge}`}>
			<span className={`relative flex ${sizeStyle.dot}`}>
				<span className={`absolute inline-flex h-full w-full animate-ping rounded-full ${config.ping} opacity-75`} />
				<span className={`relative inline-flex rounded-full ${sizeStyle.dot} ${config.dot}`} />
			</span>

			{showLabel && <span>{config.label}</span>}
		</div>
	);
}
