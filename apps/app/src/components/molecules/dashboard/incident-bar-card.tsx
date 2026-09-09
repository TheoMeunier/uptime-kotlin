import { useTranslation } from 'react-i18next';
import { TriangleAlert } from 'lucide-react';
import Metric from '@/components/molecules/metric.tsx';

interface IncidentBar {
	hour: Date;
	up_count: number;
	down_count: number;
}

interface IncidentBarCardProps {
	data: IncidentBar[];
	description?: string;
}

const CHART_HEIGHT = 56;
const SHOW_LABEL_INDEXES = [0, 6, 12, 18, 23];

export default function IncidentBarCard({ data, description }: IncidentBarCardProps) {
	const { t, i18n } = useTranslation();

	const total = data.reduce((sum, d) => sum + d.up_count + d.down_count, 0);
	const maxVal = Math.max(...data.map((d) => d.up_count + d.down_count), 1);
	const hasIncident = data.some((d) => d.down_count > 0);

	const fmtHour = (date: Date) =>
		new Date(date).toLocaleTimeString(i18n.language, { hour: '2-digit', minute: '2-digit' });

	return (
		<Metric
			label={t('dashboard.title.incidents')}
			value={total.toLocaleString(i18n.language)}
			description={description}
			icon={TriangleAlert}
			state={hasIncident ? 'down' : 'neutral'}
		>
			<div className="flex items-end gap-[3px]" style={{ height: CHART_HEIGHT }}>
				{data.map((d, i) => {
					const totalH = d.up_count + d.down_count;
					const barH = Math.max((totalH / maxVal) * CHART_HEIGHT, 2);
					const upH = Math.max((d.up_count / Math.max(totalH, 1)) * barH, 1);
					const downH = d.down_count > 0 ? Math.max((d.down_count / totalH) * barH, 1) : 0;

					return (
						<div
							key={i}
							className="group flex flex-1 cursor-default flex-col justify-end transition-opacity hover:opacity-60"
							title={`${fmtHour(d.hour)} — ${t('dashboard.legend.up')}: ${d.up_count} · ${t('dashboard.legend.down')}: ${d.down_count}`}
						>
							<div className="bg-status-up rounded-t-sm" style={{ height: upH }} />
							{downH > 0 && <div className="bg-status-down" style={{ height: downH }} />}
						</div>
					);
				})}
			</div>

			<div className="mt-1 flex justify-between px-px">
				{data.map((d, i) =>
					SHOW_LABEL_INDEXES.includes(i) ? (
						<span key={i} className="text-muted-foreground tabular text-[9px]">
							{fmtHour(d.hour)}
						</span>
					) : null
				)}
			</div>

			<div className="mt-1.5 flex gap-2.5">
				<span className="flex items-center gap-1">
					<span className="bg-status-up h-1.5 w-1.5 rounded-sm" />
					<span className="text-muted-foreground text-[10px]">{t('dashboard.legend.up')}</span>
				</span>
				<span className="flex items-center gap-1">
					<span className="bg-status-down h-1.5 w-1.5 rounded-sm" />
					<span className="text-muted-foreground text-[10px]">{t('dashboard.legend.down')}</span>
				</span>
			</div>
		</Metric>
	);
}
