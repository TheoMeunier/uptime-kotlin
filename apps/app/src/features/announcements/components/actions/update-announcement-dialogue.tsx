import { useTranslation } from 'react-i18next';
import { Pencil } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Dialog, DialogContent, DialogTrigger } from '@/components/atoms/dialog.tsx';
import { ScrollArea } from '@/components/atoms/scroll-area.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import AnnouncementForm from '@/features/announcements/components/forms/announcement-form.tsx';
import useStoreAnnouncement from '@/features/announcements/hooks/useStoreAnnouncement.ts';
import { useAnnouncement } from '@/features/announcements/hooks/useAnnouncements.ts';
import { toFormValues } from '@/features/announcements/hooks/useAnnouncementForm.ts';

export default function UpdateAnnouncementDialogue({ announcementId }: { announcementId: string }) {
	const { t } = useTranslation();
	const { open, setOpen, onSubmit, isLoading } = useStoreAnnouncement(announcementId);
	const { data, isFetching } = useAnnouncement(announcementId, open);

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger asChild>
				<Button variant="outline" size="sm" aria-label={t('button.actions.edit')}>
					<Pencil className="size-4" />
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-2xl">
				<ScrollArea className="max-h-[75vh] pr-4">
					{isFetching || !data ? (
						<div className="space-y-4 p-1">
							<Skeleton className="h-6 w-48" />
							<Skeleton className="h-10 w-full" />
							<Skeleton className="h-24 w-full" />
							<Skeleton className="h-10 w-full" />
						</div>
					) : (
						<AnnouncementForm
							mode="update"
							defaultValues={toFormValues(data)}
							onSubmit={onSubmit}
							isLoading={isLoading}
						/>
					)}
				</ScrollArea>
			</DialogContent>
		</Dialog>
	);
}
