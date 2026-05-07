import ProbeForm from '@/features/probes/components/forms/probe-form.tsx';
import ProbeProtocol from '@/features/probes/enums/probe-enum.ts';
import { useStoreMonitor } from '@/features/probes/hooks/useStoreProbeForm.ts';

export default function CreateProbe() {
	const { isLoading, onsubmit } = useStoreMonitor();

	const defaultValues = {
		protocol: ProbeProtocol.HTTP,
		interval: 60,
		interval_retry: 60,
		retry: 3,
		timeout: 30,
		enabled: true,
		notifications: [],
	};

	return (
		<>
			<div className="mb-8">
				<h1 className="scroll-m-20 border-b pb-2 text-3xl font-semibold tracking-tight first:mt-0">Create monitor</h1>
			</div>

			<ProbeForm
				mode="create"
				onSubmit={onsubmit}
				cancelLink={'/dashboard'}
				isLoading={isLoading}
				defaultValues={defaultValues}
			/>
		</>
	);
}
