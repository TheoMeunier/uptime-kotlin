import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import { z } from 'zod';

const ProbeMonitorsSchema = z.array(
	z.object({
		id: z.uuid(),
		status: z.enum(ProbeStatusEnum),
		response_time: z.number(),
		message: z.string(),
		run_at: z.date(),
	})
);

export type ProbeMonitors = z.infer<typeof ProbeMonitorsSchema>;
export default ProbeMonitorsSchema;
