import { useTranslation } from 'react-i18next';
import { Trash2 } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import ConfirmDialogue from '@/components/molecules/confirm-dialogue.tsx';
import { useDeleteStatusPage } from '@/features/status-pages/hooks/useStatusPages.ts';
import type { StatusPageListItem } from '@/features/status-pages/schemas/status-page.schema.ts';

export default function DeleteStatusPageDialogue({ page }: { page: StatusPageListItem }) {
	const { t } = useTranslation();
	const remove = useDeleteStatusPage();

	return (
		<ConfirmDialogue
			icon={Trash2}
			title={t('status_pages.title.remove')}
			description={t('status_pages.description.remove', { title: page.title, slug: page.slug })}
			confirmLabel={t('button.actions.remove')}
			isPending={remove.isPending}
			onConfirm={() => remove.mutate(page.id)}
			trigger={
				<Button
					variant="outline"
					size="sm"
					aria-label={t('status_pages.title.remove')}
					className="text-status-down-fg hover:bg-status-down-bg hover:text-status-down-fg"
				>
					<Trash2 className="size-4" />
				</Button>
			}
		/>
	);
}
