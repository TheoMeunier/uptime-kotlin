import { useTranslation } from 'react-i18next';
import { CopyPlus, ShieldCheck } from 'lucide-react';
import { CONFLICT_STRATEGIES, type ConflictStrategy } from '@/features/backup/schemas/backup.schema.ts';
import { cn } from '@/lib/utils';

const ICONS: Record<ConflictStrategy, typeof ShieldCheck> = {
	skip: ShieldCheck,
	rename: CopyPlus,
};

interface ConflictStrategyChoiceProps {
	value: ConflictStrategy;
	onChange: (value: ConflictStrategy) => void;
	disabled?: boolean;
}

export default function ConflictStrategyChoice({ value, onChange, disabled }: ConflictStrategyChoiceProps) {
	const { t } = useTranslation();

	return (
		<div className="grid gap-3 sm:grid-cols-2">
			{CONFLICT_STRATEGIES.map((strategy) => {
				const Icon = ICONS[strategy];
				const checked = value === strategy;

				return (
					<label
						key={strategy}
						className={cn(
							'flex cursor-pointer items-start gap-3 rounded-lg border p-4 transition-colors',
							'has-[:focus-visible]:ring-ring/50 has-[:focus-visible]:ring-[3px]',
							checked ? 'border-primary bg-primary/5' : 'hover:bg-muted/40',
							disabled && 'pointer-events-none opacity-50'
						)}
					>
						<input
							type="radio"
							name="restore-conflict"
							value={strategy}
							checked={checked}
							disabled={disabled}
							onChange={() => onChange(strategy)}
							className="sr-only"
						/>
						<Icon className={cn('mt-0.5 size-5 shrink-0', checked ? 'text-primary' : 'text-muted-foreground')} />
						<span className="flex flex-col gap-1">
							<span className="text-sm font-medium">{t(`backup.restore.conflict_strategy.${strategy}.title`)}</span>
							<span className="text-muted-foreground text-xs">
								{t(`backup.restore.conflict_strategy.${strategy}.description`)}
							</span>
						</span>
					</label>
				);
			})}
		</div>
	);
}
