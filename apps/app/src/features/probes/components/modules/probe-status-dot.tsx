import { getStatusTokens } from '@/lib/status.ts';
import { cn } from '@/lib/utils';

export default function ProbeStatusDot({ status, className }: { status?: string; className?: string }) {
	return (
		<span
			className={cn('size-2 shrink-0 rounded-full', status ? getStatusTokens(status).solid : 'bg-muted', className)}
		/>
	);
}
