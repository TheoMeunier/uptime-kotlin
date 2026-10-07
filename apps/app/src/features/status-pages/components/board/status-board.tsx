import { useMemo } from 'react';
import { summarizeAttention } from '@/lib/status.ts';
import useTabStatus from '@/hooks/use-tab-status.ts';
import StatusBoardHeader from '@/features/status-pages/components/board/status-board-header.tsx';
import StatusEmptyState from '@/features/status-pages/components/board/status-empty-state.tsx';
import StatusGrid from '@/features/status-pages/components/board/status-grid.tsx';
import StatusList from '@/features/status-pages/components/board/status-list.tsx';
import type { StatusSection } from '@/features/status-pages/lib/status-item.ts';
import type { StatusLayout } from '@/features/status-pages/lib/status-layout.ts';

interface StatusBoardProps {
	title: string;
	subtitle?: string | null;
	sections: StatusSection[];
	layout: StatusLayout;
	onLayoutChange: (layout: StatusLayout) => void;
}

export default function StatusBoard({ title, subtitle, sections, layout, onLayoutChange }: StatusBoardProps) {
	const items = useMemo(() => sections.flatMap((section) => section.items), [sections]);
	const visibleSections = sections.filter((section) => section.items.length > 0);
	const Section = layout === 'list' ? StatusList : StatusGrid;

	const attention = useMemo(
		() => summarizeAttention(items.filter((item) => !item.maintenance).map((item) => item.probe.status)),
		[items]
	);

	useTabStatus({ ...attention, page: title });

	return (
		<div className="bg-background min-h-screen">
			<StatusBoardHeader
				title={title}
				subtitle={subtitle}
				items={items}
				layout={layout}
				onLayoutChange={onLayoutChange}
			/>

			<div className="mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 sm:py-12 lg:px-8">
				{visibleSections.length > 0 ? (
					<div className="flex flex-col gap-10">
						{visibleSections.map((section, index) => (
							<Section key={index} section={section} />
						))}
					</div>
				) : (
					<StatusEmptyState />
				)}
			</div>
		</div>
	);
}
