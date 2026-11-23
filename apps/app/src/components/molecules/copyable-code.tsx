import CopyButton from '@/components/molecules/copy-button.tsx';
import { cn } from '@/lib/utils';

interface CopyableCodeProps {
	value: string;
	label?: string;
	size?: 'sm' | 'md';
	className?: string;
}

export default function CopyableCode({ value, label, size = 'sm', className }: CopyableCodeProps) {
	return (
		<div className={cn('bg-muted flex items-center gap-2 rounded-md py-1 pr-1 pl-3', className)}>
			<code className={cn('min-w-0 flex-1 font-mono break-all', size === 'md' ? 'text-sm' : 'text-xs')}>{value}</code>
			<CopyButton value={value} label={label} />
		</div>
	);
}
