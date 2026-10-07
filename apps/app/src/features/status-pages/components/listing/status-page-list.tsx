import { useTranslation } from 'react-i18next';
import { Globe } from 'lucide-react';
import { Card, CardContent } from '@/components/atoms/card.tsx';
import StatusPageLinkRow from '@/features/status-pages/components/listing/status-page-link-row.tsx';
import StatusPageRow from '@/features/status-pages/components/listing/status-page-row.tsx';
import StatusPageRowSkeleton from '@/features/status-pages/components/listing/status-page-row-skeleton.tsx';
import type { StatusPageListItem } from '@/features/status-pages/schemas/status-page.schema.ts';

const SKELETON_ROWS = 3;

interface StatusPageListProps {
	pages?: StatusPageListItem[];
	isLoading: boolean;
}

/* The built-in `/status` comes first: it always exists and cannot be edited or deleted. */
export default function StatusPageList({ pages, isLoading }: StatusPageListProps) {
	const { t } = useTranslation();

	return (
		<Card className="py-0">
			<CardContent className="divide-y px-0">
				<StatusPageLinkRow
					icon={<Globe className="size-4" />}
					title={t('status_pages.global.title')}
					href="/status"
					meta={t('status_pages.global.description')}
				/>

				{isLoading
					? Array.from({ length: SKELETON_ROWS }).map((_, index) => <StatusPageRowSkeleton key={index} />)
					: pages?.map((page) => <StatusPageRow key={page.id} page={page} />)}
			</CardContent>
		</Card>
	);
}
