import { useLocation } from 'react-router';
import StatusBoardSkeleton from '@/features/status-pages/components/board/status-board-skeleton.tsx';
import { statusSlugFromPath } from '@/features/status-pages/lib/status-board-path.ts';
import { readStoredLayout } from '@/features/status-pages/lib/status-layout.ts';

export default function StatusBoardFallback() {
	const { pathname } = useLocation();

	return <StatusBoardSkeleton layout={readStoredLayout(statusSlugFromPath(pathname)) ?? 'grid'} />;
}
