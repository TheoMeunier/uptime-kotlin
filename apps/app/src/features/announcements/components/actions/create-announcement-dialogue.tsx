import { useTranslation } from 'react-i18next';
import { Plus } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Dialog, DialogContent, DialogTrigger } from '@/components/atoms/dialog.tsx';
import { ScrollArea } from '@/components/atoms/scroll-area.tsx';
import AnnouncementForm from '@/features/announcements/components/forms/announcement-form.tsx';
import useStoreAnnouncement from '@/features/announcements/hooks/useStoreAnnouncement.ts';

export default function CreateAnnouncementDialogue() {
	const { t } = useTranslation();
	const { open, setOpen, onSubmit, isLoading } = useStoreAnnouncement();

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger asChild>
				<Button>
					<Plus className="size-4" /> {t('announcements.actions.create')}
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-2xl">
				<ScrollArea className="max-h-[75vh] pr-4">
					<AnnouncementForm mode="create" onSubmit={onSubmit} isLoading={isLoading} />
				</ScrollArea>
			</DialogContent>
		</Dialog>
	);
}
