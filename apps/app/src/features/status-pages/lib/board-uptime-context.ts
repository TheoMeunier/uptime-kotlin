import { createContext } from 'react';
import type { UptimePeriod } from '@/features/status-pages/lib/uptime-period.ts';
import type { ProbeUptimeBar } from '@/features/status-pages/schemas/status-uptime.schema.ts';

export interface BoardUptime {
	period: UptimePeriod;
	bars?: Map<string, ProbeUptimeBar>;
}

export const BoardUptimeContext = createContext<BoardUptime>({ period: 'H1' });
