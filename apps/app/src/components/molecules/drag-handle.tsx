import { GripVertical } from 'lucide-react';
import { cn } from '@/lib/utils';

interface DragHandleProps extends React.ComponentProps<'button'> {
	label: string;
}

export default function DragHandle({ label, className, ...props }: DragHandleProps) {
	return (
		<button
			type="button"
			aria-label={label}
			className={cn(
				'text-muted-foreground hover:text-foreground focus-visible:ring-ring/50 flex size-8 shrink-0 cursor-grab touch-none items-center justify-center rounded-md outline-none focus-visible:ring-[3px] active:cursor-grabbing',
				className
			)}
			{...props}
		>
			<GripVertical className="size-4" />
		</button>
	);
}
