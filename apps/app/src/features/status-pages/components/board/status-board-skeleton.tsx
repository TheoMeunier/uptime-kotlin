import { Card, CardContent } from '@/components/atoms/card.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import { GRID_COLUMNS, LIST_COLUMNS } from '@/features/status-pages/components/board/layout-classes.ts';
import type { StatusLayout } from '@/features/status-pages/lib/status-layout.ts';
import { cn } from '@/lib/utils';

const BAR_COUNT = 30;
const ITEM_COUNT = 6;

export default function StatusBoardSkeleton({ layout }: { layout: StatusLayout }) {
	return (
		<div className="bg-background min-h-screen">
			<div className="bg-card border-b">
				<div className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
					<div className="mb-3 flex items-center gap-3">
						<Skeleton className="h-10 w-10 shrink-0 rounded-lg" />
						<div className="flex flex-col gap-2">
							<Skeleton className="h-7 w-56" />
							<Skeleton className="h-3.5 w-72" />
						</div>
					</div>
					<div className="flex items-center gap-3">
						<Skeleton className="h-3 w-44" />
						<Skeleton className="h-3 w-40" />
						<Skeleton className="h-6 w-28 rounded-full" />
					</div>
				</div>
			</div>

			<div className="mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 sm:py-12 lg:px-8">
				{layout === 'list' ? (
					<div className="flex flex-col gap-2.5 lg:pt-6">
						{Array.from({ length: ITEM_COUNT }).map((_, i) => (
							<div
								key={i}
								className={cn('bg-card flex flex-col gap-3 rounded-lg border px-4 py-4 sm:px-5', LIST_COLUMNS)}
							>
								<div className="flex flex-col gap-1.5">
									<Skeleton className="h-5 w-36" />
									<Skeleton className="h-3.5 w-48" />
								</div>
								<Skeleton className="h-6 w-full rounded-[2px]" />
							</div>
						))}
					</div>
				) : (
					<div className={GRID_COLUMNS}>
						{Array.from({ length: ITEM_COUNT }).map((_, i) => (
							<Card key={i} className="border-border bg-card">
								<CardContent>
									<div className="flex items-start justify-between gap-2">
										<div className="flex flex-col gap-1.5">
											<Skeleton className="h-4 w-32" />
											<Skeleton className="h-3 w-48" />
										</div>
										<Skeleton className="h-6 w-20 shrink-0 rounded-full" />
									</div>

									<div className="my-3 flex h-8 w-full items-end gap-[3px]">
										{Array.from({ length: BAR_COUNT }).map((_, j) => (
											<Skeleton key={j} className="h-8 flex-1 rounded-[2px]" />
										))}
									</div>

									<div className="border-border mt-4 border-t pt-3">
										<Skeleton className="h-3 w-32" />
									</div>
								</CardContent>
							</Card>
						))}
					</div>
				)}
			</div>
		</div>
	);
}
