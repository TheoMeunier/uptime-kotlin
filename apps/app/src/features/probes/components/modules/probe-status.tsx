import { useTranslation } from 'react-i18next';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import { getStatusTokens } from '@/lib/status.ts';

const sizeConfig = {
	sm: { container: 'px-2 py-1 text-xs gap-1.5', dot: 'h-2 w-2' },
	md: { container: 'px-3 py-1.5 text-sm gap-2', dot: 'h-2.5 w-2.5' },
	lg: { container: 'px-4 py-2 text-base gap-2.5', dot: 'h-3 w-3' },
};

type SizeVariant = keyof typeof sizeConfig;

interface ProbeStatusProps {
	status: ProbeStatusEnum;
	size?: SizeVariant;
	showLabel?: boolean;
}

export default function ProbeStatus({ status, size = 'md', showLabel = true }: ProbeStatusProps) {
	const { t } = useTranslation();
	const tokens = getStatusTokens(status);
	const sizeStyle = sizeConfig[size];

	/*
	 * The pulse is reserved for failures. Animating every healthy probe turned it into a visual tic
	 * that carried no information; here it means "this one wants your attention".
	 */
	const isAlerting = status === ProbeStatusEnum.FAILURE;

	return (
		<div
			className={`inline-flex items-center rounded-full font-medium ${sizeStyle.container} ${tokens.bg} ${tokens.fg}`}
		>
			<span className={`relative flex ${sizeStyle.dot}`}>
				{isAlerting && (
					<span
						className={`motion-safe:animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${tokens.solid}`}
					/>
				)}
				<span className={`relative inline-flex rounded-full ${sizeStyle.dot} ${tokens.solid}`} />
			</span>

			{showLabel && <span>{t(tokens.labelKey)}</span>}
		</div>
	);
}
