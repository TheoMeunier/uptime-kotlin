import { useSearchParams } from 'react-router';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import ProbeForm from '@/features/probes/components/forms/probe-form.tsx';
import ProbeProtocol from '@/features/probes/enums/probe-enum.ts';
import { useStoreMonitor } from '@/features/probes/hooks/useStoreProbeForm.ts';
import probeService from '@/features/probes/services/probeService.ts';
import ErrorState from '@/components/molecules/error-state.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import {
	GetProbeUpdateResponseSchema,
	type ProbeGetUpdateResponse,
} from '@/features/probes/schemas/probe-response.schema.ts';

const DEFAULT_VALUES = {
	protocol: ProbeProtocol.HTTP,
	interval: 60,
	interval_retry: 60,
	retry: 3,
	timeout: 30,
	alert_repeat_seconds: 0,
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
	const [searchParams] = useSearchParams();
	const sourceId = searchParams.get('from');

	if (sourceId) return <DuplicateProbe key={sourceId} sourceId={sourceId} />;

	return <CreateProbeForm defaultValues={DEFAULT_VALUES} cancelLink="/dashboard" />;
}

function DuplicateProbe({ sourceId }: { sourceId: string }) {
	const { t } = useTranslation();

	const { data, isLoading, isError, refetch } = useQuery({
		queryKey: ['probe-update', sourceId],
		queryFn: async () => {
			return await probeService.getProbeForUpdate<ProbeGetUpdateResponse>(sourceId, GetProbeUpdateResponseSchema);
		},
	});

	if (isLoading) {
		return (
			<header className="mb-6 flex flex-col gap-2">
				<Skeleton className="h-7 w-48" />
				<Skeleton className="h-4 w-72" />
			</header>
		);
	}
	if (isError || !data) return <ErrorState onRetry={() => refetch()} />;

	const sourceName = data.probe.name;
	const defaultValues = {
		notifications: data.notifications,
		...data.probe,
		...data.probe?.content,
		name: t('monitors.duplicate.copy_name', { name: sourceName }),
	};

	return (
		<>
			<header className="mb-6">
				<h1 className="text-2xl font-semibold tracking-tight">{t('monitors.title.duplicate')}</h1>
				<p className="text-muted-foreground mt-1 text-sm">
					{t('monitors.duplicate.description', { name: sourceName })}
				</p>
			</header>

			<CreateProbeForm defaultValues={defaultValues} cancelLink={`/monitors/${sourceId}`} />
		</>
	);
}

function CreateProbeForm({
	defaultValues,
	cancelLink,
}: {
	defaultValues: Parameters<typeof ProbeForm>[0]['defaultValues'];
	cancelLink: string;
}) {
	const { isLoading, onsubmit } = useStoreMonitor();

	return (
		<ProbeForm
			mode="create"
			onSubmit={onsubmit}
			cancelLink={cancelLink}
			isLoading={isLoading}
			defaultValues={defaultValues}
		/>
	);
}
