import { useQuery } from '@tanstack/react-query';
import { Link, useLocation } from 'react-router';
import { useTranslation } from 'react-i18next';
import { ApiError } from '@/api/api-error.ts';
import { Button } from '@/components/atoms/button.tsx';
import ErrorPage from '@/components/molecules/error-page.tsx';
import probeService from '@/features/probes/services/probeService.ts';
import ErrorState from '@/components/molecules/error-state.tsx';
import authService from '@/features/auth/services/authServices.ts';
import { loginPath } from '@/features/auth/lib/redirect.ts';
import StatusBoard from '@/features/status-pages/components/board/status-board.tsx';
import StatusBoardFrame from '@/features/status-pages/components/board/status-board-frame.tsx';
import StatusBoardSkeleton from '@/features/status-pages/components/board/status-board-skeleton.tsx';
import useStatusLayout from '@/features/status-pages/hooks/useStatusLayout.ts';
import useStatusFeed from '@/features/status-pages/hooks/useStatusFeed.ts';
import { STATUS_QUERY_TIMING } from '@/features/status-pages/lib/status-refresh.ts';

const isNotFound = (error: unknown) => error instanceof ApiError && error.status === 404;

export default function ProbesStatus() {
	const { t } = useTranslation();
	const location = useLocation();
	const [layout, setLayout] = useStatusLayout('grid');
	const { data, isLoading, isError, error, refetch } = useQuery({
		queryKey: ['probes-status'],
		queryFn: async () => {
			return probeService.getProbesStatus();
		},
		...STATUS_QUERY_TIMING,
		retry: (failureCount, failure) => !isNotFound(failure) && failureCount < 3,
		meta: { silentError: true },
	});
	const { data: feed } = useStatusFeed(undefined, Boolean(data));

	if (isLoading) return <StatusBoardSkeleton layout={layout} />;

	if (isNotFound(error)) {
		return (
			<ErrorPage
				code="404"
				title={t('pages.status_page.not_found.title')}
				description={t('pages.status_page.not_found.description_global')}
				actions={
					!authService.getAccessToken() && (
						<Button asChild>
							<Link to={loginPath(location.pathname + location.search)}>{t('button.login')}</Link>
						</Button>
					)
				}
			/>
		);
	}

	if (isError) {
		return (
			<StatusBoardFrame>
				<ErrorState onRetry={() => refetch()} />
			</StatusBoardFrame>
		);
	}

	return (
		<StatusBoard
			title={t('pages.status_page.title')}
			subtitle={t('pages.status_page.subtitle')}
			sections={[{ items: data ?? [] }]}
			layout={layout}
			onLayoutChange={setLayout}
			feed={feed}
		/>
	);
}
