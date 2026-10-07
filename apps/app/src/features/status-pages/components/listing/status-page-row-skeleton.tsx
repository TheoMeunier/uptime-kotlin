import { Skeleton } from '@/components/atoms/skeleton.tsx';

export default function StatusPageRowSkeleton() {
	return (
		<div className="flex items-center gap-4 px-4 py-4 sm:px-6">
			<Skeleton className="size-9 rounded-lg" />
			<div className="flex flex-1 flex-col gap-1.5">
				<Skeleton className="h-4 w-40" />
				<Skeleton className="h-3 w-56" />
			</div>
			<Skeleton className="h-8 w-20" />
		</div>
	);
}
