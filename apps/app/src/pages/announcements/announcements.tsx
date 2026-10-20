import { useTranslation } from 'react-i18next';
import { Card, CardContent } from '@/components/atoms/card.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import ErrorState from '@/components/molecules/error-state.tsx';
import useAnnouncements from '@/features/announcements/hooks/useAnnouncements.ts';
import CreateAnnouncementDialogue from '@/features/announcements/components/actions/create-announcement-dialogue.tsx';
import ListingAnnouncements from '@/features/announcements/components/listing/listing-announcements.tsx';
import AnnouncementsEmptyState from '@/features/announcements/components/listing/announcements-empty-state.tsx';

export default function Announcements() {
	const { t } = useTranslation();
	const { data, isLoading, isError, refetch } = useAnnouncements();

	if (isError) return <ErrorState onRetry={() => refetch()} />;

	return (
		<div className="space-y-4">
			<section className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
				<div className="min-w-0">
					<h1 className="truncate text-2xl font-semibold tracking-tight">{t('announcements.title.index')}</h1>
					<p className="text-muted-foreground mt-1 text-sm">{t('announcements.description.index')}</p>
				</div>

				<div className="shrink-0">
					<CreateAnnouncementDialogue />
				</div>
			</section>

			<Card>
				<CardContent>
					{isLoading ? (
						<div className="space-y-3">
							{Array.from({ length: 3 }).map((_, index) => (
								<div key={index} className="flex items-center gap-4">
									<Skeleton className="h-4 flex-1" />
									<Skeleton className="h-4 w-32" />
									<Skeleton className="h-4 w-24" />
									<Skeleton className="h-8 w-24" />
								</div>
							))}
						</div>
					) : data && data.length > 0 ? (
						<ListingAnnouncements announcements={data} />
					) : (
						<AnnouncementsEmptyState />
					)}
				</CardContent>
			</Card>
		</div>
	);
}
