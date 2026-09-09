import { useParams } from 'react-router';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import probeService from '@/features/probes/services/probeService.ts';
import ProbeForm from '@/features/probes/components/forms/probe-form.tsx';
import useUpdateMonitor from '@/features/probes/hooks/useUpdateMonitor.ts';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import { Card, CardContent, CardHeader } from '@/components/atoms/card.tsx';
import {
	GetProbeUpdateResponseSchema,
	type ProbeGetUpdateResponse,
} from '@/features/probes/schemas/probe-response.schema.ts';

export default function EditProbe() {
	const params = useParams();
	const { t } = useTranslation();

	const { data, isLoading } = useQuery({
		queryKey: ['probe-update', params.probeId!],
		queryFn: async () => {
			return await probeService.getProbeForUpdate<ProbeGetUpdateResponse>(
				params.probeId!,
				GetProbeUpdateResponseSchema
			);
		},
	});

	if (isLoading || !data) return <EditProbeSkeleton />;

	const flattenedData = { notifications: data.notifications, ...data.probe, ...data.probe?.content };

	return (
		<>
			<header className="mb-6">
				<h1 className="text-2xl font-semibold tracking-tight">{t('monitors.title.update')}</h1>
				<p className="text-muted-foreground mt-1 text-sm">{data.probe.name}</p>
			</header>

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

function EditProbeSkeleton() {
	return (
		<>
			<header className="mb-6 flex flex-col gap-2">
				<Skeleton className="h-7 w-48" />
				<Skeleton className="h-4 w-32" />
			</header>

			<div className="grid gap-6 lg:grid-cols-3">
				<div className="flex flex-col gap-6 lg:col-span-2">
					{[4, 3].map((rows, i) => (
						<Card key={i}>
							<CardHeader>
								<Skeleton className="h-4 w-32" />
							</CardHeader>
							<CardContent className="flex flex-col gap-4">
								{Array.from({ length: rows }).map((_, j) => (
									<div key={j} className="flex flex-col gap-2">
										<Skeleton className="h-3 w-24" />
										<Skeleton className="h-9 w-full" />
									</div>
								))}
							</CardContent>
						</Card>
					))}
				</div>

				<div className="flex flex-col gap-6">
					{[1, 3].map((rows, i) => (
						<Card key={i}>
							<CardHeader>
								<Skeleton className="h-4 w-28" />
							</CardHeader>
							<CardContent className="flex flex-col gap-4">
								{Array.from({ length: rows }).map((_, j) => (
									<div key={j} className="flex flex-col gap-2">
										<Skeleton className="h-3 w-20" />
										<Skeleton className="h-9 w-full" />
									</div>
								))}
							</CardContent>
						</Card>
					))}
				</div>
			</div>
		</>
	);
}
