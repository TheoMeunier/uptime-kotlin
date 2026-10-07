import { GRID_COLUMNS } from '@/features/status-pages/components/board/layout-classes.ts';
import StatusGridCard from '@/features/status-pages/components/board/status-grid-card.tsx';
import type { StatusSection } from '@/features/status-pages/lib/status-item.ts';

export default function StatusGrid({ section }: { section: StatusSection }) {
	return (
		<section>
			{section.name && <h2 className="text-foreground mb-4 text-lg font-semibold tracking-tight">{section.name}</h2>}

			<div className={GRID_COLUMNS}>
				{section.items.map((item) => (
					<StatusGridCard key={item.probe.id} item={item} />
				))}
			</div>
		</section>
	);
}
