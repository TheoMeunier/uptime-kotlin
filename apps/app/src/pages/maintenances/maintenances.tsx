import { useTranslation } from 'react-i18next';
import { Wrench } from 'lucide-react';
import { Card, CardContent } from '@/components/atoms/card.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import ErrorState from '@/components/molecules/error-state.tsx';
import useMaintenances from '@/features/maintenances/hooks/useMaintenances.ts';
import ListingMaintenance from '@/features/maintenances/components/listing-maintenance.tsx';
import MaintenanceDialogue from '@/features/maintenances/components/actions/maintenance-dialogue.tsx';

export default function Maintenances() {
	const { t } = useTranslation();
	const { data, isLoading, isError, refetch } = useMaintenances();

	if (isError) return <ErrorState onRetry={() => refetch()} />;

	return (
		<div className="space-y-4">
			<section className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
				<div className="min-w-0">
					<h1 className="truncate text-2xl font-semibold tracking-tight">{t('maintenances.title.index')}</h1>
					<p className="text-muted-foreground mt-1 text-sm">{t('maintenances.description.index')}</p>
				</div>

				<div className="shrink-0">
					<MaintenanceDialogue />
				</div>
			</section>

			<Card>
				<CardContent>
					{isLoading ? (
						<div className="space-y-3">
							{Array.from({ length: 4 }).map((_, index) => (
								<div key={index} className="flex items-center gap-4">
									<Skeleton className="h-4 flex-1" />
									<Skeleton className="h-4 w-32" />
									<Skeleton className="h-4 w-20" />
									<Skeleton className="h-8 w-24" />
								</div>
							))}
						</div>
					) : data && data.length > 0 ? (
						<ListingMaintenance maintenances={data} />
					) : (
						<div className="py-16 text-center">
							<div className="bg-muted mx-auto mb-4 flex size-16 items-center justify-center rounded-full">
								<Wrench className="text-muted-foreground size-8" />
							</div>
							<h3 className="text-foreground mb-2 text-lg font-semibold">{t('maintenances.empty.title')}</h3>
							<p className="text-muted-foreground">{t('maintenances.empty.description')}</p>
						</div>
					)}
				</CardContent>
			</Card>
		</div>
	);
}
