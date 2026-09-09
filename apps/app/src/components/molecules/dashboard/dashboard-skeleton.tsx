import { Skeleton } from '@/components/atoms/skeleton.tsx';

/** Mirrors the real dashboard tile for tile, so nothing jumps when the data lands. */
function MetricSkeleton({ children }: { children?: React.ReactNode }) {
	return (
		<div className="bg-muted/40 border-border space-y-2 rounded-lg border px-4 py-3">
			<div className="flex items-center justify-between">
				<Skeleton className="h-3 w-20" />
				<Skeleton className="h-7 w-7 rounded-lg" />
			</div>
			<Skeleton className="h-7 w-16" />
			<Skeleton className="h-2.5 w-24" />
			{children}
		</div>
	);
}

export default function DashboardSkeleton() {
	return (
		<div className="space-y-6">
			<section>
				<div className="grid gap-3 md:grid-cols-2 lg:grid-cols-4">
					{Array.from({ length: 4 }).map((_, i) => (
						<MetricSkeleton key={i} />
					))}
				</div>
			</section>

			<section>
				<div className="grid gap-3 md:grid-cols-3">
					{Array.from({ length: 3 }).map((_, i) => (
						<MetricSkeleton key={i}>
							<Skeleton className="mt-1 h-12 w-full rounded-md" />
						</MetricSkeleton>
					))}
				</div>
			</section>

			<section>
				<div className="rounded-xl border p-5">
					<div className="mb-4 space-y-1.5">
						<Skeleton className="h-3.5 w-32" />
						<Skeleton className="h-2.5 w-44" />
					</div>

					<div className="space-y-3">
						{Array.from({ length: 3 }).map((_, i) => (
							<div key={i} className="flex items-center gap-4">
								<Skeleton className="h-6 w-20 rounded-full" />
								<Skeleton className="h-3.5 w-32" />
								<Skeleton className="ml-auto h-5 w-24 rounded-md" />
							</div>
						))}
					</div>
				</div>
			</section>
		</div>
	);
}
