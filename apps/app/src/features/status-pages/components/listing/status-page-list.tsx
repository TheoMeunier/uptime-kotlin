import { Card, CardContent } from '@/components/atoms/card.tsx';
import GlobalStatusPageRow from '@/features/status-pages/components/listing/global-status-page-row.tsx';
import StatusPageRow from '@/features/status-pages/components/listing/status-page-row.tsx';
import StatusPageRowSkeleton from '@/features/status-pages/components/listing/status-page-row-skeleton.tsx';
import type { StatusPageListItem } from '@/features/status-pages/schemas/status-page.schema.ts';

const SKELETON_ROWS = 3;

interface StatusPageListProps {
	pages?: StatusPageListItem[];
	isLoading: boolean;
}

export default function StatusPageList({ pages, isLoading }: StatusPageListProps) {
	return (
		<Card className="py-0">
			<CardContent className="divide-y px-0">
				<GlobalStatusPageRow />

				{isLoading
					? Array.from({ length: SKELETON_ROWS }).map((_, index) => <StatusPageRowSkeleton key={index} />)
					: pages?.map((page) => <StatusPageRow key={page.id} page={page} />)}
			</CardContent>
		</Card>
	);
}
