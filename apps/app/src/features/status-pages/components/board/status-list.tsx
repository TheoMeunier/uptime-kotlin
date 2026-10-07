import StatusListHeader from '@/features/status-pages/components/board/status-list-header.tsx';
import StatusListRow from '@/features/status-pages/components/board/status-list-row.tsx';
import type { StatusSection } from '@/features/status-pages/lib/status-item.ts';

export default function StatusList({ section }: { section: StatusSection }) {
	return (
		<section>
			{section.name && (
				<h2 className="text-foreground mb-3 text-lg font-semibold tracking-tight lg:hidden">{section.name}</h2>
			)}

			<StatusListHeader name={section.name} />

			<ul className="flex flex-col gap-2.5">
				{section.items.map((item) => (
					<StatusListRow key={item.probe.id} item={item} />
				))}
			</ul>
		</section>
	);
}
