import { Skeleton } from '@/components/atoms/skeleton.tsx';

export default function DashboardSkeleton() {
	return (
		<div className="space-y-6">
			<div className="flex items-center justify-between">
				<Skeleton className="h-7 w-36" />
			</div>

			<section>
				<div className="grid gap-3 md:grid-cols-2 lg:grid-cols-4">
					{Array.from({ length: 4 }).map((_, i) => (
						<div key={i} className="rounded-xl border p-5 space-y-3">
							<div className="flex items-center justify-between">
								<Skeleton className="h-3 w-20" />
								<Skeleton className="h-7 w-7 rounded-md" />
							</div>
							<Skeleton className="h-7 w-14" />
							<Skeleton className="h-2.5 w-28" />
						</div>
					))}
				</div>
			</section>

			<section>
				<div className="grid gap-3 md:grid-cols-3">
					<div className="rounded-xl border p-5 space-y-3">
						<div className="flex items-center justify-between">
							<Skeleton className="h-3 w-24" />
							<Skeleton className="h-6 w-6 rounded-md" />
						</div>
						<Skeleton className="h-6 w-16" />
						<Skeleton className="h-2.5 w-28" />
						<Skeleton className="h-12 w-full rounded-md mt-1" />
					</div>

					<div className="rounded-xl border p-5 space-y-3">
						<div className="space-y-1.5">
							<Skeleton className="h-3 w-24" />
							<Skeleton className="h-2.5 w-16" />
						</div>
						<div className="flex items-end gap-1 h-14 mt-1">
							{[40, 70, 55, 85, 35, 60, 100, 48].map((h, i) => (
								<Skeleton key={i} className="flex-1 rounded-sm" style={{ height: `${h}%` }} />
							))}
						</div>
					</div>

					<div className="rounded-xl border p-5 space-y-3">
						<div className="flex items-center justify-between">
							<Skeleton className="h-3 w-24" />
							<Skeleton className="h-6 w-6 rounded-md" />
						</div>
						<Skeleton className="h-6 w-16" />
						<Skeleton className="h-2.5 w-28" />
						<Skeleton className="h-12 w-full rounded-md mt-1" />
					</div>
				</div>
			</section>

			<section>
				<div className="rounded-xl border">
					<div className="flex items-center justify-between p-5 pb-3">
						<div className="space-y-1.5">
							<Skeleton className="h-3.5 w-32" />
							<Skeleton className="h-2.5 w-44" />
						</div>
						<Skeleton className="h-7 w-28 rounded-lg" />
					</div>
					<div className="px-5 pb-5">
						<div className="grid grid-cols-[96px_1fr_120px] gap-3 pb-3">
							<Skeleton className="h-2.5 w-12" />
							<Skeleton className="h-2.5 w-16" />
							<Skeleton className="h-2.5 w-12" />
						</div>
						{Array.from({ length: 3 }).map((_, i) => (
							<div key={i} className="grid grid-cols-[96px_1fr_120px] gap-3 items-center py-3 border-t">
								<Skeleton className="h-5 w-5 rounded-full" />
								<Skeleton className="h-3.5 w-32" />
								<Skeleton className="h-6 w-20 rounded-md" />
							</div>
						))}
					</div>
				</div>
			</section>
		</div>
	);
}
