import { Card, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';
import type ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';

export default function ProbeMonitorLog({ monitors }: { monitors: Monitor[] }) {
	return (
		<Card className="space-y-0">
			<CardHeader className="border-b border-gray-300 pb-4">
				<CardTitle className="text-xl font-semibold">Probe Monitor Log Messages</CardTitle>
				<CardDescription>Recent activity from your monitoring probes</CardDescription>
			</CardHeader>

			<div>
				<div className="max-h-[500px] overflow-y-auto font-mono">
					{monitors
						?.slice()
						.reverse()
						.map((monitor) => {
							const config = statusConfig[monitor.status] || statusConfig.PAUSE;
							const runAt = new Date(monitor.run_at);

							return (
								<div
									key={monitor.id}
									className={`
                  flex items-start gap-4 px-4 py-1
                  hover:bg-gray-100/50 transition-colors duration-150
                `}
								>
									<div className="flex-1 min-w-0 space-y-2">
										<div className="flex items-center gap-3 flex-wrap">
											<span
												className={`
                      font-semibold text-xs uppercase tracking-wide
                      ${config.text}
                    `}
											>
												{config.label}
											</span>
											<span className="text-xs text-zinc-500">
												{runAt.toLocaleString('fr-FR', {
													day: '2-digit',
													month: '2-digit',
													hour: '2-digit',
													minute: '2-digit',
												})}{' '}
											</span>

											<div className={`flex items-center gap-1`}>
												<span className={`text-xs font-medium text-emerald-400`}>{monitor.response_time}ms</span>
											</div>

											<p className="text-sm text-gray-500 leading-relaxed">{monitor.message || 'No message'}</p>
										</div>
									</div>
								</div>
							);
						})}
				</div>
			</div>
		</Card>
	);
}

interface StatusConfig {
	bg: string;
	border: string;
	text: string;
	label: string;
}

const statusConfig: Record<ProbeStatusEnum, StatusConfig> = {
	SUCCESS: {
		bg: 'bg-emerald-500/10',
		border: 'border-emerald-500/20',
		text: 'text-emerald-400',
		label: 'Success',
	},
	FAILURE: {
		bg: 'bg-red-500/10',
		border: 'border-red-500/20',
		text: 'text-red-400',
		label: 'Error',
	},
	WARNING: {
		bg: 'bg-amber-500/10',
		border: 'border-amber-500/20',
		text: 'text-amber-400',
		label: 'Warning',
	},
	PAUSE: {
		bg: 'bg-blue-500/10',
		border: 'border-blue-500/20',
		text: 'text-blue-400',
		label: 'Pending',
	},
};
