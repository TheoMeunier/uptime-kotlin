import { Check, Copy } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/atoms/button.tsx';
import useCopyToClipboard from '@/hooks/use-copy-to-clipboard.ts';
import { cn } from '@/lib/utils';

interface CopyableCodeProps {
	value: string;
	label?: string;
	size?: 'sm' | 'md';
	className?: string;
}

export default function CopyableCode({ value, label, size = 'sm', className }: CopyableCodeProps) {
	const { t } = useTranslation();
	const { copied, copy } = useCopyToClipboard();

	return (
		<div className={cn('bg-muted flex items-center gap-2 rounded-md py-1 pr-1 pl-3', className)}>
			<code className={cn('min-w-0 flex-1 font-mono break-all', size === 'md' ? 'text-sm' : 'text-xs')}>{value}</code>
			<Button type="button" variant="ghost" size="sm" className="h-7 shrink-0 text-xs" onClick={() => void copy(value)}>
				{copied ? <Check className="size-3.5" /> : <Copy className="size-3.5" />}
				<span aria-live="polite">{copied ? t('clipboard.copied') : (label ?? t('clipboard.copy'))}</span>
			</Button>
		</div>
	);
}
