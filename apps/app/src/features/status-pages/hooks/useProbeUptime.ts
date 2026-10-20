import { useContext } from 'react';
import { BoardUptimeContext } from '@/features/status-pages/lib/board-uptime-context.ts';

export default function useProbeUptime(probeId?: string) {
	const { period, bars } = useContext(BoardUptimeContext);

	return { period, bar: probeId ? bars?.get(probeId) : undefined, isLoading: !bars };
}
