import { useParams } from 'react-router';
import { useQuery } from '@tanstack/react-query';
import probeService from '@/features/probes/services/probeService.ts';
import ProbeForm from '@/features/probes/components/forms/probe-form.tsx';
import useUpdateMonitor from '@/features/probes/hooks/useUpdateMonitor.ts';
import {
	GetProbeUpdateResponseSchema,
	type ProbeGetUpdateResponse,
} from '@/features/probes/schemas/probe-response.schema.ts';

export default function EditProbe() {
	const params = useParams();

	const { data, isLoading } = useQuery({
		queryKey: ['probe-update', params.probeId!],
		queryFn: async () => {
			return await probeService.getProbe<ProbeGetUpdateResponse>(params.probeId!, 0, GetProbeUpdateResponseSchema);
		},
	});

	if (isLoading && !data) return <div>Loading...</div>;

	const flattenedData = { notifications: data?.notifications, ...data?.probe, ...data?.probe?.content };

	return (
		<>
			<div className="mb-8">
				<h1 className="scroll-m-20 border-b pb-2 text-3xl font-semibold tracking-tight first:mt-0">
					Update monitor: {flattenedData.name}
				</h1>
			</div>

			<FormUpdateProbe data={flattenedData} probeId={params.probeId!} />
		</>
	);
}

function FormUpdateProbe({ data, probeId }: { data: ProbeGetUpdateResponse; probeId: string }) {
	const { isLoading, onsubmit } = useUpdateMonitor(probeId);

	return (
		<ProbeForm
			mode="edit"
			defaultValues={data}
			cancelLink={`/monitors/${probeId}`}
			onSubmit={onsubmit}
			isLoading={isLoading}
		/>
	);
}
