import { Link } from 'react-router';
import { useTranslation } from 'react-i18next';
import { LayoutGrid, LayoutList, Pencil } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import DeleteStatusPageDialogue from '@/features/status-pages/components/actions/delete-status-page-dialogue.tsx';
import StatusPageLinkRow from '@/features/status-pages/components/listing/status-page-link-row.tsx';
import type { StatusPageListItem } from '@/features/status-pages/schemas/status-page.schema.ts';

export default function StatusPageRow({ page }: { page: StatusPageListItem }) {
	const { t } = useTranslation();
	const LayoutIcon = page.default_layout === 'LIST' ? LayoutList : LayoutGrid;

	return (
		<StatusPageLinkRow
			icon={<LayoutIcon className="size-4" />}
			title={page.title}
			href={`/status/${page.slug}`}
			meta={[
				t('status_pages.count.groups', { count: page.group_count }),
				t('status_pages.count.probes', { count: page.probe_count }),
			].join(' · ')}
			actions={
				<>
					<Button variant="outline" size="sm" asChild>
						<Link to={`/status-pages/${page.id}`}>
							<Pencil className="size-4" />
							<span className="hidden sm:inline">{t('button.actions.edit')}</span>
						</Link>
					</Button>
					<DeleteStatusPageDialogue page={page} />
				</>
			}
		/>
	);
}
