import { useTranslation } from 'react-i18next';
import { CheckCircle2, TriangleAlert, XCircle } from 'lucide-react';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';

interface StatusVerdictProps {
	statuses: ProbeStatusEnum[];
}

/**
 * A visitor arrives with one question: is it broken? Answering it with a grid of cards makes them
 * count. This states the answer before the detail.
 */
export default function StatusVerdict({ statuses }: StatusVerdictProps) {
	const { t } = useTranslation();

	const total = statuses.length;
	if (total === 0) return null;

	const down = statuses.filter((s) => s === ProbeStatusEnum.FAILURE).length;
	const degraded = statuses.filter((s) => s === ProbeStatusEnum.WARNING).length;

	const { Icon, tone, accent, message } =
		down > 0
			? {
					Icon: XCircle,
					tone: 'bg-status-down-bg text-status-down-fg',
					accent: 'bg-status-down',
					message: t('pages.status_page.verdict.down', { count: down, total }),
				}
			: degraded > 0
				? {
						Icon: TriangleAlert,
						tone: 'bg-status-degraded-bg text-status-degraded-fg',
						accent: 'bg-status-degraded',
						message: t('pages.status_page.verdict.degraded', { count: degraded, total }),
					}
				: {
						Icon: CheckCircle2,
						tone: 'bg-status-up-bg text-status-up-fg',
						accent: 'bg-status-up',
						message: t('pages.status_page.verdict.operational', { count: total }),
					};

	return (
		<div className={`flex items-center gap-3 overflow-hidden rounded-lg ${tone}`}>
			<span className={`h-full w-1 self-stretch ${accent}`} aria-hidden="true" />
			<span className="flex items-center gap-3 py-4 pr-4">
				<Icon className="size-5 shrink-0" />
				<span className="text-base font-semibold">{message}</span>
			</span>
		</div>
	);
}
