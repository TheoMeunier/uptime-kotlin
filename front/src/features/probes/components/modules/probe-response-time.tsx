import { Card, CardContent, CardDescription, CardTitle } from '@/components/atoms/card.tsx';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/atoms/select.tsx';
import { useTranslation } from 'react-i18next';
import type { Monitor } from '@/features/probes/schemas/probe-monitor.schema.ts';
import type ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import ProbeChart from '@/features/probes/components/modules/probe-chart.tsx';
import ResponseTimeStats from '@/features/probes/components/modules/probe-response-time-status.tsx';

interface ProbeResponseTimeProps {
	monitors: Monitor[];
	lastHour: number;
	setLastHour: (value: number) => void;
	monitorStatus: ProbeStatusEnum;
}

export default function ProbeResponseTime({ monitors, monitorStatus, lastHour, setLastHour }: ProbeResponseTimeProps) {
	const { t } = useTranslation();

	const getTimeRangeLabel = () => {
		switch (lastHour) {
			case 1:
				return t('timeRanger.last_1_hour');
			case 3:
				return t('timeRanger.last_3_hours');
			case 6:
				return t('timeRanger.last_6_hours');
			case 24:
				return t('timeRanger.last_24_hours');
			case 168:
				return t('timeRanger.last_7_days');
			default:
				return '';
		}
	};

	return (
		<Card>
			<CardContent>
				<div className="flex items-center gap-2 space-y-0 sm:flex-row">
					<div className="grid flex-1 gap-1">
						<CardTitle>Response Time</CardTitle>
						<CardDescription>Showing probe response times for {getTimeRangeLabel()}</CardDescription>
					</div>
					<Select value={lastHour.toString()} onValueChange={(value) => setLastHour(Number(value))}>
						<SelectTrigger className="hidden w-[160px] rounded-lg sm:ml-auto sm:flex" aria-label="Select a value">
							<SelectValue placeholder="Select time range" />
						</SelectTrigger>
						<SelectContent className="rounded-xl">
							<SelectItem value="1" className="rounded-lg">
								{t('timeRanger.last_1_hour')}
							</SelectItem>
							<SelectItem value="3" className="rounded-lg">
								{t('timeRanger.last_3_hours')}
							</SelectItem>
							<SelectItem value="6" className="rounded-lg">
								{t('timeRanger.last_6_hours')}
							</SelectItem>
							<SelectItem value="24" className="rounded-lg">
								{t('timeRanger.last_24_hours')}
							</SelectItem>
							<SelectItem value="168" className="rounded-lg">
								{t('timeRanger.last_7_days')}
							</SelectItem>
						</SelectContent>
					</Select>
				</div>
				<div className="space-y-8">
					<ResponseTimeStats monitors={monitors} />
					<ProbeChart monitors={monitors} lastHour={lastHour} monitorStatus={monitorStatus} />
				</div>
			</CardContent>
		</Card>
	);
}
