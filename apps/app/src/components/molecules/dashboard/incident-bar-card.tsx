import { CardTitle } from '@/components/atoms/card.tsx';
import { TriangleAlert } from 'lucide-react';

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

function fmt(date: Date) {
	return `${date.getHours()}h`;
}

export default function IncidentBarCard({ data, description = 'Sur 24 heures' }: IncidentBarCardProps) {
	const total = data.reduce((s, d) => s + d.up_count + d.down_count, 0);
	const maxVal = Math.max(...data.map((d) => d.up_count + d.down_count), 1);

	return (
		<div className="bg-gray-50 border border-gray-100 rounded-lg py-3 px-4">
			<div className="flex flex-row items-center justify-between mb-2">
				<CardTitle className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Incidents</CardTitle>
				<div className="h-7 w-7 rounded-lg flex items-center justify-center bg-muted text-muted-foreground">
					<TriangleAlert className="h-3.5 w-3.5" />
				</div>
			</div>

			<div className="text-2xl font-semibold text-foreground">{total.toLocaleString()}</div>
			{description && <p className="text-xs text-muted-foreground mt-0.5">{description}</p>}

			<div className="mt-2">
				<div className="flex items-end gap-[3px]" style={{ height: CHART_HEIGHT }}>
					{data.map((d, i) => {
						const totalH = d.up_count + d.down_count;
						const barH = Math.max((totalH / maxVal) * CHART_HEIGHT, 2);
						const upH = Math.max((d.up_count / Math.max(totalH, 1)) * barH, 1);
						const downH = d.down_count > 0 ? Math.max((d.down_count / totalH) * barH, 1) : 0;

						return (
							<div
								key={i}
								className="flex flex-col justify-end flex-1 group cursor-default"
								title={`${fmt(d.hour)} — up: ${d.up_count} · down: ${d.down_count}`}
							>
								<div
									className="rounded-t-sm bg-green-400 group-hover:bg-green-500 transition-colors"
									style={{ height: upH }}
								/>

								{downH > 0 && (
									<div className="bg-red-400 group-hover:bg-red-500 transition-colors" style={{ height: downH }} />
								)}
							</div>
						);
					})}
				</div>

				<div className="flex justify-between mt-1 px-px">
					{data.map((d, i) =>
						SHOW_LABEL_INDEXES.includes(i) ? (
							<span key={i} className="text-[9px] text-gray-300">
								{fmt(d.hour)}
							</span>
						) : null
					)}
				</div>

				<div className="flex gap-2.5 mt-1.5">
					<div className="flex items-center gap-1">
						<div className="w-1.5 h-1.5 rounded-sm bg-green-400" />
						<span className="text-[10px] text-muted-foreground">Up</span>
					</div>

					<div className="flex items-center gap-1">
						<div className="w-1.5 h-1.5 rounded-sm bg-red-400" />
						<span className="text-[10px] text-muted-foreground">Down</span>
					</div>
				</div>
			</div>
		</div>
	);
}
