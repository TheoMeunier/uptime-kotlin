import { Skeleton } from '@/components/atoms/skeleton.tsx';

export default function StatusPageFormSkeleton() {
	return (
		<div className="grid gap-4 lg:grid-cols-[minmax(0,22rem)_minmax(0,1fr)]">
			<Skeleton className="h-96 w-full rounded-lg" />
			<Skeleton className="h-96 w-full rounded-lg" />
		</div>
	);
}
