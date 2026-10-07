import { useQuery } from '@tanstack/react-query';
import { useParams } from 'react-router';
import { useTranslation } from 'react-i18next';
import { ApiError } from '@/api/api-error.ts';
import ErrorState from '@/components/molecules/error-state.tsx';
import StatusBoard from '@/features/status-pages/components/board/status-board.tsx';
import StatusBoardFrame from '@/features/status-pages/components/board/status-board-frame.tsx';
import StatusBoardSkeleton from '@/features/status-pages/components/board/status-board-skeleton.tsx';
import useStatusLayout from '@/features/status-pages/hooks/useStatusLayout.ts';
import { fromApiLayout, readStoredLayout } from '@/features/status-pages/lib/status-layout.ts';
import statusPageService from '@/features/status-pages/services/status-page-service.ts';

const isNotFound = (error: unknown) => error instanceof ApiError && error.status === 404;

export default function PublicStatusPage() {
	const { t } = useTranslation();
	const { slug = '' } = useParams();

	const { data, isLoading, isError, error, refetch } = useQuery({
		queryKey: ['status-page-public', slug],
		queryFn: () => statusPageService.getPublicStatusPage(slug),
		refetchInterval: 120000,
		retry: (failureCount, failure) => !isNotFound(failure) && failureCount < 3,
		meta: { silentError: true },
	});

	const [layout, setLayout] = useStatusLayout(data ? fromApiLayout(data.default_layout) : 'grid', slug);

	if (isLoading) return <StatusBoardSkeleton layout={readStoredLayout(slug) ?? 'grid'} />;

	if (isError || !data) {
		return (
			<StatusBoardFrame>
				{isNotFound(error) ? (
					<ErrorState
						title={t('pages.status_page.not_found.title')}
						description={t('pages.status_page.not_found.description', { slug })}
					/>
				) : (
					<ErrorState onRetry={() => refetch()} />
				)}
			</StatusBoardFrame>
		);
	}

	return (
		<StatusBoard
			title={data.title}
			subtitle={data.description}
			sections={data.groups}
			layout={layout}
			onLayoutChange={setLayout}
		/>
	);
}
