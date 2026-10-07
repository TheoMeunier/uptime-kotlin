import { useTranslation } from 'react-i18next';
import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import SortableProbe from '@/features/status-pages/components/editor/sortable-probe.tsx';
import type { EditorProbe } from '@/features/status-pages/lib/editor.ts';
import { dropDndId, probeDndId } from '@/features/status-pages/lib/dnd-ids.ts';
import { cn } from '@/lib/utils';

interface GroupProbeListProps {
	groupKey: string;
	probeIds: string[];
	probes: Map<string, EditorProbe>;
	onRemove: (probeId: string) => void;
}

/* The probes of one group. The whole list is a drop zone, so an empty group can still receive one. */
export default function GroupProbeList({ groupKey, probeIds, probes, onRemove }: GroupProbeListProps) {
	const { t } = useTranslation();
	const { setNodeRef, isOver } = useDroppable({ id: dropDndId(groupKey) });
	const isEmpty = probeIds.length === 0;

	return (
		<SortableContext items={probeIds.map(probeDndId)} strategy={verticalListSortingStrategy}>
			<ul
				ref={setNodeRef}
				className={cn(
					'flex min-h-12 flex-col gap-1.5 rounded-md transition-colors',
					isEmpty && 'border border-dashed',
					isEmpty && isOver && 'border-primary bg-primary/5'
				)}
			>
				{isEmpty && (
					<li className="text-muted-foreground flex min-h-12 items-center justify-center px-3 text-center text-sm">
						{t('status_pages.form.empty_group')}
					</li>
				)}

				{probeIds.map((id) => (
					<SortableProbe key={id} id={id} probe={probes.get(id)} onRemove={() => onRemove(id)} />
				))}
			</ul>
		</SortableContext>
	);
}
