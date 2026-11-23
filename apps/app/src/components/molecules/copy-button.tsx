import { Check, Copy } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/atoms/button.tsx';
import useCopyToClipboard from '@/hooks/use-copy-to-clipboard.ts';
import { cn } from '@/lib/utils';

interface CopyButtonProps {
	value: string;
	label?: string;
	variant?: 'ghost' | 'outline';
	className?: string;
}

export default function CopyButton({ value, label, variant = 'ghost', className }: CopyButtonProps) {
	const { t } = useTranslation();
	const { copied, copy } = useCopyToClipboard();

	return (
		<Button
			type="button"
			variant={variant}
			size="sm"
			className={cn(variant === 'ghost' && 'h-7 shrink-0 text-xs', className)}
			onClick={() => void copy(value)}
		>
			{copied ? <Check className="size-3.5" /> : <Copy className="size-3.5" />}
			<span aria-live="polite">{copied ? t('clipboard.copied') : (label ?? t('clipboard.copy'))}</span>
		</Button>
	);
}
