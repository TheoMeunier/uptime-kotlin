import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, useLocation, useParams } from 'react-router';
import { RotateCw } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { ApiError } from '@/api/api-error.ts';
import { Button } from '@/components/atoms/button.tsx';
import ErrorPage from '@/components/molecules/error-page.tsx';
import authService from '@/features/auth/services/authServices.ts';
import { loginPath } from '@/features/auth/lib/redirect.ts';
import StatusBoard from '@/features/status-pages/components/board/status-board.tsx';
import StatusBoardSkeleton from '@/features/status-pages/components/board/status-board-skeleton.tsx';
import useStatusLayout from '@/features/status-pages/hooks/useStatusLayout.ts';
import useStatusFeed from '@/features/status-pages/hooks/useStatusFeed.ts';
import useStatusUptime from '@/features/status-pages/hooks/useStatusUptime.ts';
import type { BoardUptime } from '@/features/status-pages/lib/board-uptime-context.ts';
import { fromApiLayout, readStoredLayout } from '@/features/status-pages/lib/status-layout.ts';
import statusPageService from '@/features/status-pages/services/status-page-service.ts';

const isNotFound = (error: unknown) => error instanceof ApiError && error.status === 404;

export default function PublicStatusPage() {
	const { t } = useTranslation();
	const { slug = '' } = useParams();
	const location = useLocation();

	const { data, isLoading, isError, error, refetch } = useQuery({
		queryKey: ['status-page-public', slug],
		queryFn: () => statusPageService.getPublicStatusPage(slug),
		refetchInterval: 120000,
		retry: (failureCount, failure) => !isNotFound(failure) && failureCount < 3,
		meta: { silentError: true },
	});

	const { data: feed } = useStatusFeed(slug, Boolean(data));
	const period = data?.uptime_period;
	const { data: uptimeData } = useStatusUptime(slug, period);
	const uptime = useMemo<BoardUptime>(
		() => ({
			period: period ?? 'H1',
			bars:
				uptimeData && uptimeData.period === period
					? new Map(uptimeData.probes.map((bar) => [bar.probe_id, bar]))
					: undefined,
		}),
		[period, uptimeData]
	);

	const [layout, setLayout] = useStatusLayout(data ? fromApiLayout(data.default_layout) : 'grid', slug);

	if (isLoading) return <StatusBoardSkeleton layout={readStoredLayout(slug) ?? 'grid'} />;

	if (isError || !data) {
		if (isNotFound(error)) {
			return (
				<ErrorPage
					code="404"
					title={t('pages.status_page.not_found.title')}
					description={t('pages.status_page.not_found.description', { slug })}
					actions={
						<>
							{!authService.getAccessToken() && (
								<Button asChild>
									<Link to={loginPath(location.pathname + location.search)}>{t('button.login')}</Link>
								</Button>
							)}
							<Button variant="outline" asChild>
								<Link to="/status">{t('errors.actions.status')}</Link>
							</Button>
						</>
					}
				/>
			);
		}

		return (
			<ErrorPage
				code="500"
				title={t('errors.load.title')}
				description={t('errors.load.description')}
				actions={
					<Button onClick={() => refetch()}>
						<RotateCw className="size-4" />
						{t('button.retry')}
					</Button>
				}
			/>
		);
	}

	return (
		<StatusBoard
			title={data.title}
			subtitle={data.description}
			sections={data.groups}
			layout={layout}
			onLayoutChange={setLayout}
			feed={feed}
			uptime={uptime}
		/>
	);
}
