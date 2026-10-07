import {
	type DragEndEvent,
	type DragOverEvent,
	KeyboardSensor,
	PointerSensor,
	type UniqueIdentifier,
	useSensor,
	useSensors,
} from '@dnd-kit/core';
import { arrayMove, sortableKeyboardCoordinates } from '@dnd-kit/sortable';
import type { EditorGroup } from '@/features/status-pages/lib/editor.ts';
import {
	dropKeyOf,
	groupKeyOf,
	isDropDndId,
	isGroupDndId,
	isProbeDndId,
	probeIdOf,
} from '@/features/status-pages/lib/dnd-ids.ts';

export default function useGroupsDragAndDrop(groups: EditorGroup[], onChange: (groups: EditorGroup[]) => void) {
	const sensors = useSensors(
		useSensor(PointerSensor, { activationConstraint: { distance: 4 } }),
		useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
	);

	const groupKeyAt = (id: UniqueIdentifier): string | undefined => {
		if (isGroupDndId(id)) return groupKeyOf(id);
		if (isDropDndId(id)) return dropKeyOf(id);
		if (isProbeDndId(id)) return groups.find((group) => group.probeIds.includes(probeIdOf(id)))?.key;

		return undefined;
	};

	const onDragOver = ({ active, over }: DragOverEvent) => {
		if (!over || !isProbeDndId(active.id)) return;

		const from = groupKeyAt(active.id);
		const to = groupKeyAt(over.id);
		if (!from || !to || from === to) return;

		const moved = probeIdOf(active.id);
		const target = groups.find((group) => group.key === to);
		if (!target) return;

		const overIndex = isProbeDndId(over.id) ? target.probeIds.indexOf(probeIdOf(over.id)) : -1;
		const insertAt = overIndex < 0 ? target.probeIds.length : overIndex;

		onChange(
			groups.map((group) => {
				if (group.key === from) return { ...group, probeIds: group.probeIds.filter((id) => id !== moved) };
				if (group.key === to) {
					const probeIds = [...group.probeIds];
					probeIds.splice(insertAt, 0, moved);
					return { ...group, probeIds };
				}

				return group;
			})
		);
	};

	const onDragEnd = ({ active, over }: DragEndEvent) => {
		if (!over || active.id === over.id) return;

		if (isGroupDndId(active.id)) {
			const from = groups.findIndex((group) => group.key === groupKeyOf(active.id));
			const to = groups.findIndex((group) => group.key === groupKeyAt(over.id));
			if (from >= 0 && to >= 0 && from !== to) onChange(arrayMove(groups, from, to));

			return;
		}

		const key = groupKeyAt(active.id);
		if (!key || !isProbeDndId(over.id) || key !== groupKeyAt(over.id)) return;

		onChange(
			groups.map((group) => {
				if (group.key !== key) return group;

				const from = group.probeIds.indexOf(probeIdOf(active.id));
				const to = group.probeIds.indexOf(probeIdOf(over.id));

				return from >= 0 && to >= 0 ? { ...group, probeIds: arrayMove(group.probeIds, from, to) } : group;
			})
		);
	};

	return { sensors, onDragOver, onDragEnd };
}
