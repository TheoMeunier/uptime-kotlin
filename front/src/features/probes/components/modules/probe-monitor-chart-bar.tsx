import { HoverCard, HoverCardContent, HoverCardTrigger } from '@/components/atoms/hover-card';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';

type BarItem = Monitor | { type: 'pause'; id: string; timestamp: Date };

export default function ProbeMonitorChartBar({ monitors, probeStatus }: { monitors: Monitor[]; probeStatus: string }) {
	const formatTime = (dateString: string) => {
		const date = new Date(dateString);
		return date.toLocaleTimeString('fr-FR', {
			hour: '2-digit',
			minute: '2-digit',
			second: '2-digit',
		});
	};

	const formatDate = (dateString: string) => {
		const date = new Date(dateString);
		return date.toLocaleDateString('fr-FR', {
			day: 'numeric',
			month: 'long',
			year: 'numeric',
		});
	};

	const getBarColor = (status: string) => {
		switch (status) {
			case 'SUCCESS':
				return 'bg-green-400';
			case 'PAUSE':
				return 'bg-gray-400';
			case 'WARNING':
				return 'bg-yellow-400';
			default:
				return 'bg-red-400';
		}
	};

	const getStatusBadgeColor = (status: string) => {
		switch (status) {
			case 'SUCCESS':
				return 'bg-green-500/20 text-green-400';
			case 'PAUSE':
				return 'bg-gray-500/20 text-gray-400';
			case 'WARNING':
				return 'bg-yellow-500/20 text-yellow-400';
			default:
				return 'bg-red-500/20 text-red-400';
		}
	};

	const getStatusLabel = (status: string) => {
		switch (status) {
			case 'SUCCESS':
				return 'Succès';
			case 'PAUSE':
				return 'En pause';
			case 'WARNING':
				return 'Warning';
			default:
				return 'Échec';
		}
	};

	const BAR_COUNT = 60;
	const isPaused = probeStatus === 'PAUSE';

	const buildBarsWithGaps = (): BarItem[] => {
		const allBars: BarItem[] = [];
		const sortedMonitors = [...monitors].sort((a, b) => new Date(a.run_at).getTime() - new Date(b.run_at).getTime());

		for (let i = 0; i < sortedMonitors.length; i++) {
			const currentLog = sortedMonitors[i];

			if (i > 0) {
				const previousLog = sortedMonitors[i - 1];
				const previousTime = new Date(previousLog.run_at).getTime();
				const currentTime = new Date(currentLog.run_at).getTime();
				const minutesDiff = Math.floor((currentTime - previousTime) / (60 * 1000));

				if (minutesDiff > 1) {
					for (let j = 1; j < minutesDiff; j++) {
						allBars.push({
							type: 'pause',
							id: `pause-${i}-${j}`,
							timestamp: new Date(previousTime + j * 60 * 1000),
						});
					}
				}
			}

			allBars.push(currentLog);
		}

		if (isPaused && sortedMonitors.length > 0) {
			const lastLog = sortedMonitors[sortedMonitors.length - 1];
			const lastLogTime = new Date(lastLog.run_at).getTime();
			const minutesSinceLastLog = Math.floor((Date.now() - lastLogTime) / (60 * 1000));

			if (minutesSinceLastLog > 1) {
				for (let j = 1; j < minutesSinceLastLog; j++) {
					allBars.push({
						type: 'pause',
						id: `pause-current-${j}`,
						timestamp: new Date(lastLogTime + j * 60 * 1000),
					});
				}
			}
		}

		return allBars;
	};

	const allBars = buildBarsWithGaps();
	const bars = allBars.slice(-BAR_COUNT);
	const emptyBars = BAR_COUNT - bars.length;

	const renderHoverContent = (status: string, message: string, date: string, responseTime?: number) => (
		<HoverCardContent className="w-80 border-gray-300">
			<div className="space-y-2">
				<div className="flex items-center justify-between">
					<h4 className="text-sm font-semibold">{getStatusLabel(status)}</h4>
					<span className={`text-xs px-2 py-1 rounded-full ${getStatusBadgeColor(status)}`}>{status}</span>
				</div>
				<div className="text-sm">{message}</div>
				{responseTime !== undefined && (
					<div className="flex items-center gap-2 text-xs text-slate-400">
						<span className="text-blue-400 font-medium">{responseTime}ms</span>
						<span>•</span>
						<span>Response time</span>
					</div>
				)}
				<div className="pt-2 border-t border-gray-300">
					<div className="text-xs text-slate-400">{formatDate(date)}</div>
					<div className="text-xs text-slate-500">{formatTime(date)}</div>
				</div>
			</div>
		</HoverCardContent>
	);

	return (
		<div className="w-full">
			<div className="flex gap-[2px] my-3 h-8 items-end w-full">
				{Array.from({ length: emptyBars }).map((_, i) => (
					<div key={`empty-${i}`} className="h-8 flex-1 rounded bg-gray-500/40" />
				))}

				{bars.map((item) => {
					if ('type' in item && item.type === 'pause') {
						return (
							<HoverCard key={item.id} openDelay={100}>
								<HoverCardTrigger asChild>
									<div className="h-8 flex-1 rounded cursor-pointer transition-all hover:scale-110 bg-gray-400" />
								</HoverCardTrigger>
								<HoverCardContent className="w-80 border-gray-300">
									<div className="space-y-2">
										<div className="flex items-center justify-between">
											<h4 className="text-sm font-semibold">Pause</h4>
											<span className="text-xs px-2 py-1 rounded-full bg-gray-500/20 text-gray-400">PAUSED</span>
										</div>
										<div className="text-sm">Le moniteur était en pause, aucune vérification effectuée.</div>
										<div className="pt-2 border-t border-gray-300">
											<div className="text-xs text-slate-400">{formatDate(item.timestamp.toString())}</div>
											<div className="text-xs text-slate-500">{formatTime(item.timestamp.toString())}</div>
										</div>
									</div>
								</HoverCardContent>
							</HoverCard>
						);
					}

					const check = item as Monitor;
					return (
						<HoverCard key={check.id} openDelay={100}>
							<HoverCardTrigger asChild>
								<div
									className={`h-8 flex-1 rounded cursor-pointer transition-all hover:scale-110 ${getBarColor(check.status)}`}
								/>
							</HoverCardTrigger>
							{renderHoverContent(
								check.status,
								check.message,
								check.run_at.toString(),
								check.status === 'SUCCESS' ? check.response_time : undefined
							)}
						</HoverCard>
					);
				})}
			</div>
		</div>
	);
}
