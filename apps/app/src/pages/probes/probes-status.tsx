import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import probeService from '@/features/probes/services/probeService.ts';
import ErrorState from '@/components/molecules/error-state.tsx';
import StatusBoard from '@/features/status-pages/components/board/status-board.tsx';
import StatusBoardFrame from '@/features/status-pages/components/board/status-board-frame.tsx';
import StatusBoardSkeleton from '@/features/status-pages/components/board/status-board-skeleton.tsx';
import useStatusLayout from '@/features/status-pages/hooks/useStatusLayout.ts';

export default function ProbesStatus() {
	const { t } = useTranslation();
	const [layout, setLayout] = useStatusLayout('grid');
	const { data, isLoading, isError, refetch } = useQuery({
		queryKey: ['probes-status'],
		queryFn: async () => {
			return probeService.getProbesStatus();
		},
		refetchInterval: 120000,
	});

	if (isLoading) return <StatusBoardSkeleton layout={layout} />;

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
		/>
	);
}
