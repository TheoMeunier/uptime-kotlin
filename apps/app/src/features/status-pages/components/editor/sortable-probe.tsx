import { useTranslation } from 'react-i18next';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { X } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import DragHandle from '@/components/molecules/drag-handle.tsx';
import ProbeStatusDot from '@/features/probes/components/modules/probe-status-dot.tsx';
import type { EditorProbe } from '@/features/status-pages/lib/editor.ts';
import { probeDndId } from '@/features/status-pages/lib/dnd-ids.ts';
import { cn } from '@/lib/utils';

interface SortableProbeProps {
	id: string;
	probe?: EditorProbe;
	onRemove: () => void;
}

export default function SortableProbe({ id, probe, onRemove }: SortableProbeProps) {
	const { t } = useTranslation();
	const { attributes, listeners, setNodeRef, setActivatorNodeRef, transform, transition, isDragging } = useSortable({
		id: probeDndId(id),
	});
	const name = probe?.name ?? t('status_pages.form.unknown_probe');

	return (
		<li
			ref={setNodeRef}
			style={{ transform: CSS.Translate.toString(transform), transition }}
			className={cn(
				'bg-background flex items-center gap-2 rounded-md border px-1.5 py-1.5',
				isDragging && 'relative z-10 shadow-md'
			)}
		>
			<DragHandle
				ref={setActivatorNodeRef}
				label={t('status_pages.form.move_probe', { name })}
				className="size-7"
				{...attributes}
				{...listeners}
			/>

			<ProbeStatusDot status={probe?.status} />

			<div className="min-w-0 flex-1">
				<p className="truncate text-sm font-medium">{name}</p>
				{probe?.detail && <p className="text-muted-foreground truncate text-xs">{probe.detail}</p>}
			</div>

			<Button
				type="button"
				variant="ghost"
				size="icon"
				onClick={onRemove}
				aria-label={t('status_pages.form.remove_probe', { name })}
				title={t('status_pages.form.remove_probe', { name })}
				className="text-muted-foreground size-7 shrink-0"
			>
				<X className="size-4" />
			</Button>
		</li>
	);
}
