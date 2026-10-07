import { useTranslation } from 'react-i18next';
import { closestCorners, DndContext } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Plus } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import SortableGroup from '@/features/status-pages/components/editor/sortable-group.tsx';
import useGroupsDragAndDrop from '@/features/status-pages/hooks/useGroupsDragAndDrop.ts';
import { type EditorGroup, type EditorProbe, emptyGroup } from '@/features/status-pages/lib/editor.ts';
import { groupDndId } from '@/features/status-pages/lib/dnd-ids.ts';

interface GroupsEditorProps {
	groups: EditorGroup[];
	probes: Map<string, EditorProbe>;
	onChange: (groups: EditorGroup[]) => void;
}

export default function GroupsEditor({ groups, probes, onChange }: GroupsEditorProps) {
	const { t } = useTranslation();
	const { sensors, onDragOver, onDragEnd } = useGroupsDragAndDrop(groups, onChange);

	/* A probe appears once per page: what is already placed is not offered again. */
	const placed = new Set(groups.flatMap((group) => group.probeIds));
	const available = [...probes.values()].filter((probe) => !placed.has(probe.id));

	return (
		<div className="flex flex-col gap-3">
			<DndContext sensors={sensors} collisionDetection={closestCorners} onDragOver={onDragOver} onDragEnd={onDragEnd}>
				<SortableContext items={groups.map((group) => groupDndId(group.key))} strategy={verticalListSortingStrategy}>
					{groups.map((group) => (
						<SortableGroup
							key={group.key}
							group={group}
							probes={probes}
							available={available}
							onChange={(next) => onChange(groups.map((item) => (item.key === next.key ? next : item)))}
							onRemove={() => onChange(groups.filter((item) => item.key !== group.key))}
						/>
					))}
				</SortableContext>
			</DndContext>

			<Button
				type="button"
				variant="outline"
				className="self-start"
				onClick={() => onChange([...groups, emptyGroup()])}
			>
				<Plus className="size-4" />
				{t('status_pages.form.add_group')}
			</Button>
		</div>
	);
}
