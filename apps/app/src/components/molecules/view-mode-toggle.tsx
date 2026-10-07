import { useTranslation } from 'react-i18next';
import { LayoutGrid, LayoutList } from 'lucide-react';
import { ToggleGroup, ToggleGroupItem } from '@/components/atoms/toggle-group.tsx';
import { cn } from '@/lib/utils';

export type ViewMode = 'grid' | 'list';

const MODES = [
	{ value: 'grid', icon: LayoutGrid, labelKey: 'pages.status_page.layout.grid' },
	{ value: 'list', icon: LayoutList, labelKey: 'pages.status_page.layout.list' },
] as const;

interface ViewModeToggleProps {
	'value': ViewMode;
	'onChange': (value: ViewMode) => void;
	'showLabels'?: boolean;
	'className'?: string;
	'aria-label'?: string;
	'aria-labelledby'?: string;
}

export default function ViewModeToggle({
	value,
	onChange,
	showLabels = false,
	className,
	...aria
}: ViewModeToggleProps) {
	const { t } = useTranslation();

	return (
		<ToggleGroup
			type="single"
			spacing={1}
			value={value}
			onValueChange={(next) => {
				if (next === 'grid' || next === 'list') onChange(next);
			}}
			className={cn('bg-muted/60 rounded-lg p-1', showLabels && 'w-full', className)}
			{...aria}
		>
			{MODES.map(({ value: mode, icon: Icon, labelKey }) => (
				<ToggleGroupItem
					key={mode}
					value={mode}
					aria-label={t(labelKey)}
					title={showLabels ? undefined : t(labelKey)}
					className={cn(
						'data-[state=on]:bg-background data-[state=on]:text-foreground data-[state=on]:shadow-sm',
						showLabels ? 'h-8 flex-1 gap-2' : 'h-7 w-7 min-w-0 px-0'
					)}
				>
					<Icon className="size-4" />
					{showLabels && t(labelKey)}
				</ToggleGroupItem>
			))}
		</ToggleGroup>
	);
}
