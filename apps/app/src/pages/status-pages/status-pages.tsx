import { Link } from 'react-router';
import { useTranslation } from 'react-i18next';
import { Plus } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import ErrorState from '@/components/molecules/error-state.tsx';
import StatusPageList from '@/features/status-pages/components/listing/status-page-list.tsx';
import StatusPagesEmptyState from '@/features/status-pages/components/listing/status-pages-empty-state.tsx';
import { useStatusPages } from '@/features/status-pages/hooks/useStatusPages.ts';

export default function StatusPages() {
	const { t } = useTranslation();
	const { data, isLoading, isError, refetch } = useStatusPages();

	if (isError) return <ErrorState onRetry={() => refetch()} />;

	return (
		<div className="space-y-4">
			<section className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
				<div className="min-w-0">
					<h1 className="truncate text-2xl font-semibold tracking-tight">{t('status_pages.title.index')}</h1>
					<p className="text-muted-foreground mt-1 text-sm">{t('status_pages.description.index')}</p>
				</div>

				<Button asChild className="shrink-0">
					<Link to="/status-pages/new">
						<Plus className="size-4" />
						{t('status_pages.actions.create')}
					</Link>
				</Button>
			</section>

			<StatusPageList pages={data} isLoading={isLoading} />

			{!isLoading && data?.length === 0 && <StatusPagesEmptyState />}
		</div>
	);
}
