import { useState } from 'react';
import { Card, CardDescription, CardTitle } from '@/components/atoms/card.tsx';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import { Button } from '@/components/atoms/button.tsx';
import { Badge } from '@/components/atoms/badge.tsx';
import PurgeProbeLogsDialogue from '@/features/probes/components/actions/purge-probe-logs-dialogue.tsx';

interface StatusConfig {
	badge: string;
	dot: string;
	label: string;
}

const statusConfig: Record<ProbeStatusEnum, StatusConfig> = {
	SUCCESS: {
		badge: 'bg-green-100 text-green-700',
		dot: 'bg-green-500',
		label: 'Success',
	},
	FAILURE: {
		badge: 'bg-red-100 text-red-700',
		dot: 'bg-red-500',
		label: 'Error',
	},
	WARNING: {
		badge: 'bg-orange-100 text-orange-700',
		dot: 'bg-orange-500',
		label: 'Warning',
	},
	PAUSE: {
		badge: 'bg-gray-50 text-gray-800 border border-gray-100',
		dot: 'bg-gray-700',
		label: 'Pending',
	},
};

const filters = [
	{ key: 'all', label: 'All' },
	{ key: ProbeStatusEnum.SUCCESS, label: 'Success' },
	{ key: ProbeStatusEnum.FAILURE, label: 'Errors' },
] as const;

type FilterKey = 'all' | ProbeStatusEnum;

function getMsColor(ms: number) {
	if (ms >= 300) return 'text-red-700';
	if (ms >= 150) return 'text-orange-700';
	return 'text-green-700';
}

export default function ProbeMonitorLog({ probeId, monitors }: { probeId: string; monitors: Monitor[] }) {
	const [activeFilter, setActiveFilter] = useState<FilterKey>('all');

	const sorted = monitors?.slice().reverse() ?? [];

	const filtered = activeFilter === 'all' ? sorted : sorted.filter((m) => m.status === activeFilter);

	const countByStatus = (status: ProbeStatusEnum) => sorted.filter((m) => m.status === status).length;

	return (
		<Card className="space-y-0">
			<div className={'px-4'}>
				<div className="flex items-center justify-between">
					<div>
						<CardTitle className="text-base font-medium">Logs monitoring</CardTitle>
						<CardDescription>Recent monitor activity</CardDescription>
					</div>
					<div className="flex items-center gap-3">
						<PurgeProbeLogsDialogue probeId={probeId} disabled={sorted.length === 0} />
						<div className="flex items-center gap-1.5">
							<span className="w-2 h-2 rounded-full bg-green-700" />
							<span className="text-xs text-green-700 font-medium">{sorted.length} entrées</span>
						</div>
					</div>
				</div>
			</div>

			<div className="flex items-center gap-2 px-4 pb-2.5 border-b border-gray-100">
				{filters.map(({ key, label }) => {
					const count = key === 'all' ? sorted.length : countByStatus(key);
					return (
						<Button
							key={key}
							variant={activeFilter === key ? 'secondary' : 'outline'}
							size="sm"
							onClick={() => setActiveFilter(key)}
							className="rounded-md h-7 text-xs gap-1.5 cursor-pointer"
						>
							{label}
							<Badge variant="secondary" className="text-[10px] h-4 px-1.5">
								{count}
							</Badge>
						</Button>
					);
				})}
			</div>

			<div className="max-h-[500px] overflow-y-auto font-mono">
				{filtered.map((monitor) => {
					const config = statusConfig[monitor.status] ?? statusConfig.PAUSE;
					const runAt = new Date(monitor.run_at);

					return (
						<div
							key={monitor.id}
							className="flex items-center gap-3 px-4 py-2.5 border-b border-gray-50 hover:bg-gray-50 transition-colors"
						>
							<span
								className={`inline-flex items-center gap-1.5 text-xs font-medium px-2.5 py-1 rounded-full shrink-0 ${config.badge}`}
							>
								<span className={`w-1.5 h-1.5 rounded-full ${config.dot}`} />
								{config.label}
							</span>

							<span className="text-xs text-gray-400 tabular-nums shrink-0">
								{runAt.toLocaleString('fr-FR', {
									day: '2-digit',
									month: '2-digit',
									hour: '2-digit',
									minute: '2-digit',
								})}
							</span>

							<span className={`text-xs font-medium tabular-nums shrink-0 ${getMsColor(monitor.response_time)}`}>
								{monitor.response_time}ms
							</span>

							<p className="text-xs text-gray-500 truncate">{monitor.message || 'No message'}</p>
						</div>
					);
				})}

				{filtered.length === 0 && (
					<div className="py-12 text-center text-sm text-gray-400">No logs for this filter</div>
				)}
			</div>
		</Card>
	);
}
