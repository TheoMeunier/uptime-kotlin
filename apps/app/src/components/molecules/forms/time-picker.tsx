import { useEffect, useMemo, useRef, useState } from 'react';
import { ClockIcon } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/atoms/popover.tsx';
import { ScrollArea } from '@/components/atoms/scroll-area.tsx';
import { formatTime } from '@/lib/datetime.ts';
import { cn } from '@/lib/utils';

const pad = (value: number) => String(value).padStart(2, '0');

const HOURS_24 = Array.from({ length: 24 }, (_, hour) => ({ value: hour, label: pad(hour) }));
const HOURS_12 = [12, ...Array.from({ length: 11 }, (_, index) => index + 1)].map((hour) => ({
	value: hour % 12,
	label: pad(hour),
}));
const MINUTES = Array.from({ length: 60 }, (_, minute) => ({ value: minute, label: pad(minute) }));

/** Whether the language writes times on a 12-hour clock, the same rule `formatTime` follows. */
function uses12Hours(locale: string): boolean {
	const cycle = new Intl.DateTimeFormat(locale, { hour: 'numeric' }).resolvedOptions().hourCycle;
	return cycle === 'h11' || cycle === 'h12';
}

/** The language's own AM and PM labels. */
function dayPeriods(locale: string): [string, string] {
	const label = (hour: number) =>
		new Intl.DateTimeFormat(locale, { hour: 'numeric', hour12: true })
			.formatToParts(new Date(2000, 0, 1, hour))
			.find((part) => part.type === 'dayPeriod')?.value ?? (hour < 12 ? 'AM' : 'PM');

	return [label(9), label(21)];
}

interface TimeOption {
	value: number;
	label: string;
}

function TimeColumn({
	options,
	selected,
	label,
	open,
	onSelect,
	className,
}: {
	options: TimeOption[];
	selected?: number;
	label: string;
	open: boolean;
	onSelect: (value: number) => void;
	className?: string;
}) {
	const selectedRef = useRef<HTMLButtonElement>(null);

	// Bring the current value into view each time the popover opens.
	useEffect(() => {
		if (!open) return;
		const frame = requestAnimationFrame(() => selectedRef.current?.scrollIntoView({ block: 'center' }));
		return () => cancelAnimationFrame(frame);
	}, [open]);

	return (
		<ScrollArea className={cn('h-56 w-16', className)}>
			<div role="listbox" aria-label={label} className="flex flex-col gap-1 p-1 pr-3">
				{options.map((option) => {
					const isSelected = option.value === selected;
					return (
						<Button
							key={option.value}
							ref={isSelected ? selectedRef : undefined}
							type="button"
							role="option"
							aria-selected={isSelected}
							variant={isSelected ? 'default' : 'ghost'}
							size="sm"
							className="tabular shrink-0 font-normal"
							onClick={() => onSelect(option.value)}
						>
							{option.label}
						</Button>
					);
				})}
			</div>
		</ScrollArea>
	);
}

interface TimePickerProps {
	/** `HH:mm` on a 24-hour clock, or an empty string when no time is set. */
	value: string;
	onChange: (value: string) => void;
	onBlur?: () => void;
	/** The app's language: it decides between a 12-hour clock with AM/PM and a 24-hour one. */
	locale: string;
	invalid?: boolean;
	label: string;
	hourLabel: string;
	minuteLabel: string;
	periodLabel: string;
	placeholder?: string;
	className?: string;
}

/**
 * A time picker in the app's design, replacing the browser's native `<input type="time">`: a
 * button showing the time as the rest of the app does (`formatTime`), and a popover with hour and
 * minute columns, plus an AM/PM column for languages on a 12-hour clock. The value itself is
 * always `HH:mm` on a 24-hour clock.
 */
export default function TimePicker({
	value,
	onChange,
	onBlur,
	locale,
	invalid,
	label,
	hourLabel,
	minuteLabel,
	periodLabel,
	placeholder = '--:--',
	className,
}: TimePickerProps) {
	const [open, setOpen] = useState(false);
	const twelveHours = useMemo(() => uses12Hours(locale), [locale]);
	const periods = useMemo(() => dayPeriods(locale), [locale]);

	const match = value.match(/^(\d{2}):(\d{2})$/);
	const hour = match ? Number(match[1]) : undefined;
	const minute = match ? Number(match[2]) : undefined;
	const isPm = hour !== undefined && hour >= 12;

	const emit = (nextHour: number, nextMinute: number) => onChange(`${pad(nextHour)}:${pad(nextMinute)}`);
	const display =
		hour !== undefined && minute !== undefined ? formatTime(new Date(2000, 0, 1, hour, minute), locale) : '';

	return (
		<Popover
			open={open}
			onOpenChange={(next) => {
				setOpen(next);
				if (!next) onBlur?.();
			}}
		>
			<PopoverTrigger asChild>
				<Button
					type="button"
					variant="outline"
					aria-label={display ? `${label}: ${display}` : label}
					aria-invalid={invalid}
					className={cn(
						'tabular shrink-0 justify-between font-normal',
						twelveHours ? 'w-32' : 'w-28',
						!display && 'text-muted-foreground',
						className
					)}
				>
					{display || placeholder}
					<ClockIcon className="text-muted-foreground size-4" />
				</Button>
			</PopoverTrigger>
			<PopoverContent className="flex w-auto gap-1 p-1" align="end">
				<TimeColumn
					options={twelveHours ? HOURS_12 : HOURS_24}
					selected={hour === undefined ? undefined : twelveHours ? hour % 12 : hour}
					label={hourLabel}
					open={open}
					onSelect={(next) => emit(twelveHours && isPm ? next + 12 : next, minute ?? 0)}
				/>
				<div className="bg-border w-px" />
				<TimeColumn
					options={MINUTES}
					selected={minute}
					label={minuteLabel}
					open={open}
					onSelect={(next) => {
						emit(hour ?? 0, next);
						setOpen(false);
						onBlur?.();
					}}
				/>
				{twelveHours && (
					<>
						<div className="bg-border w-px" />
						<TimeColumn
							className="h-auto"
							options={periods.map((period, index) => ({ value: index, label: period }))}
							selected={hour === undefined ? undefined : isPm ? 1 : 0}
							label={periodLabel}
							open={open}
							onSelect={(next) => emit(((hour ?? 0) % 12) + (next === 1 ? 12 : 0), minute ?? 0)}
						/>
					</>
				)}
			</PopoverContent>
		</Popover>
	);
}
