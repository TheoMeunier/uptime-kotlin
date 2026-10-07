import { getStatusTokens } from '@/lib/status.ts';
import { cn } from '@/lib/utils';

/* The status colour alone, for dense lists where a full badge would be noise. */
export default function ProbeStatusDot({ status, className }: { status?: string; className?: string }) {
	return (
		<span
			className={cn('size-2 shrink-0 rounded-full', status ? getStatusTokens(status).solid : 'bg-muted', className)}
		/>
	);
}
