import { useTranslation } from 'react-i18next';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Trash2 } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Input } from '@/components/atoms/input.tsx';
import DragHandle from '@/components/molecules/drag-handle.tsx';
import AddProbePopover from '@/features/status-pages/components/editor/add-probe-popover.tsx';
import GroupProbeList from '@/features/status-pages/components/editor/group-probe-list.tsx';
import type { EditorGroup, EditorProbe } from '@/features/status-pages/lib/editor.ts';
import { groupDndId } from '@/features/status-pages/lib/dnd-ids.ts';
import { cn } from '@/lib/utils';

interface SortableGroupProps {
	group: EditorGroup;
	probes: Map<string, EditorProbe>;
	available: EditorProbe[];
	onChange: (group: EditorGroup) => void;
	onRemove: () => void;
}

export default function SortableGroup({ group, probes, available, onChange, onRemove }: SortableGroupProps) {
	const { t } = useTranslation();
	const { attributes, listeners, setNodeRef, setActivatorNodeRef, transform, transition, isDragging } = useSortable({
		id: groupDndId(group.key),
	});

	return (
		<div
			ref={setNodeRef}
			style={{ transform: CSS.Translate.toString(transform), transition }}
			className={cn('bg-card rounded-lg border', isDragging && 'relative z-10 opacity-80 shadow-lg')}
		>
			<div className="flex items-center gap-2 border-b p-2 sm:p-3">
				<DragHandle
					ref={setActivatorNodeRef}
					label={t('status_pages.form.move_group')}
					{...attributes}
					{...listeners}
				/>

				<Input
					value={group.name}
					onChange={(event) => onChange({ ...group, name: event.target.value })}
					placeholder={t('status_pages.form.group_name_placeholder')}
					aria-label={t('status_pages.form.group_name')}
					maxLength={255}
					className="h-8 font-medium"
				/>

				<Button
					type="button"
					variant="ghost"
					size="icon"
					onClick={onRemove}
					aria-label={t('status_pages.form.remove_group')}
					title={t('status_pages.form.remove_group')}
					className="text-muted-foreground hover:text-status-down-fg size-8 shrink-0"
				>
					<Trash2 className="size-4" />
				</Button>
			</div>

			<div className="p-2 sm:p-3">
				<GroupProbeList
					groupKey={group.key}
					probeIds={group.probeIds}
					probes={probes}
					onRemove={(id) => onChange({ ...group, probeIds: group.probeIds.filter((item) => item !== id) })}
				/>

				<AddProbePopover
					available={available}
					onSelect={(id) => onChange({ ...group, probeIds: [...group.probeIds, id] })}
				/>
			</div>
		</div>
	);
}
