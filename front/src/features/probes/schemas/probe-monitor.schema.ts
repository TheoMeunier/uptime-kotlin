import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import { z } from 'zod';

const MonitorSchema = z.object({
	id: z.uuid(),
	status: z.enum(ProbeStatusEnum),
	response_time: z.number(),
	message: z.string(),
	run_at: z.string(),
});

export type Monitor = z.infer<typeof MonitorSchema>;
export { MonitorSchema };

const ProbeMonitorsSchema = z.array(
	z.object({
		id: z.uuid(),
		status: z.enum(ProbeStatusEnum),
		response_time: z.number(),
		message: z.string(),
		run_at: z.string(),
	})
);

export type ProbeMonitors = z.infer<typeof ProbeMonitorsSchema>;
export default ProbeMonitorsSchema;
