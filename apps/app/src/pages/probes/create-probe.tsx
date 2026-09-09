import ProbeForm from '@/features/probes/components/forms/probe-form.tsx';
import ProbeProtocol from '@/features/probes/enums/probe-enum.ts';
import { useStoreMonitor } from '@/features/probes/hooks/useStoreProbeForm.ts';

const DEFAULT_VALUES = {
	protocol: ProbeProtocol.HTTP,
	interval: 60,
	interval_retry: 60,
	retry: 3,
	timeout: 30,
	enabled: true,
	notifications: [],
	method: 'GET' as const,
	headers: {},
	assertions: [],
	steps: [],
	follow_redirects: true,
	max_latency_ms: 2000,
	tls_expiry_warning_days: 30 as const,
};

export default function CreateProbe() {
	const { isLoading, onsubmit } = useStoreMonitor();

	return (
		<ProbeForm
			mode="create"
			onSubmit={onsubmit}
			cancelLink="/dashboard"
			isLoading={isLoading}
			defaultValues={DEFAULT_VALUES}
		/>
	);
}
