import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Controller, type FieldValues, type Path, type PathValue, type UseFormReturn } from 'react-hook-form';
import { CalendarIcon } from 'lucide-react';
import { de, enUS, es, fr } from 'react-day-picker/locale';
import { Button } from '@/components/atoms/button.tsx';
import { Calendar } from '@/components/atoms/calendar.tsx';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/atoms/popover.tsx';
import TimePicker from '@/components/molecules/forms/time-picker.tsx';
import { toDatetimeLocal } from '@/lib/datetime.ts';
import { cn } from '@/lib/utils';

const LOCALES = { de, en: enUS, es, fr } as const;

/** Splits a `YYYY-MM-DDTHH:mm` wall-clock value into its day (local midnight) and time. */
function split(value: string | undefined): { day?: Date; time: string } {
	const match = value?.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}:\d{2})/);
	if (!match) return { time: '' };

	const [, year, month, date, time] = match;
	return { day: new Date(Number(year), Number(month) - 1, Number(date)), time };
}

/** `YYYY-MM-DDTHH:mm`, the same format a `datetime-local` input produces. */
function join(day: Date, time: string): string {
	return `${toDatetimeLocal(day).slice(0, 10)}T${time}`;
}

interface FormDateTimePickerProps<TFieldValues extends FieldValues> {
	form: UseFormReturn<TFieldValues>;
	name: Path<TFieldValues>;
	/** Id of the day button, for the field's `<FieldLabel htmlFor>`. */
	id?: string;
	/** Earliest selectable value, in the same `YYYY-MM-DDTHH:mm` format; days before it are disabled. */
	min?: string;
	invalid?: boolean;
}

/**
 * A date and time picker in the app's design: a calendar in a popover for the day, and a time
 * field next to it. Its value is a `YYYY-MM-DDTHH:mm` wall-clock string, like the native
 * `datetime-local` input it replaces, so schemas and services keep working unchanged.
 */
export default function FormDateTimePicker<TFieldValues extends FieldValues>({
	form,
	name,
	id,
	min,
	invalid,
}: FormDateTimePickerProps<TFieldValues>) {
	const { t, i18n } = useTranslation();
	const [open, setOpen] = useState(false);
	const language = i18n.resolvedLanguage ?? i18n.language;
	const locale = LOCALES[language.split('-')[0] as keyof typeof LOCALES] ?? enUS;
	const minDay = split(min).day;

	return (
		<Controller
			name={name}
			control={form.control}
			render={({ field, fieldState }) => {
				const { day, time } = split(field.value as string | undefined);
				const isInvalid = invalid ?? fieldState.invalid;
				const update = (value: string) => field.onChange(value as PathValue<TFieldValues, Path<TFieldValues>>);

				return (
					<div className="flex gap-2">
						<Popover open={open} onOpenChange={setOpen}>
							<PopoverTrigger asChild>
								<Button
									id={id}
									type="button"
									variant="outline"
									aria-invalid={isInvalid}
									className={cn('min-w-0 flex-1 justify-between font-normal', !day && 'text-muted-foreground')}
								>
									<span className="truncate">
										{day ? day.toLocaleDateString(language, { dateStyle: 'medium' }) : t('datetime_picker.pick_date')}
									</span>
									<CalendarIcon className="text-muted-foreground size-4" />
								</Button>
							</PopoverTrigger>
							<PopoverContent className="w-auto overflow-hidden p-0" align="start">
								<Calendar
									mode="single"
									locale={locale}
									formatters={{
										formatMonthDropdown: (month) => month.toLocaleString(language, { month: 'short' }),
									}}
									selected={day}
									defaultMonth={day ?? minDay}
									captionLayout="dropdown"
									startMonth={minDay ?? new Date(new Date().getFullYear() - 5, 0)}
									endMonth={new Date(new Date().getFullYear() + 10, 11)}
									disabled={minDay ? { before: minDay } : undefined}
									onSelect={(selected) => {
										if (!selected) return;
										update(join(selected, time || toDatetimeLocal(new Date()).slice(11)));
										setOpen(false);
										field.onBlur();
									}}
								/>
							</PopoverContent>
						</Popover>

						<TimePicker
							label={t('datetime_picker.time')}
							hourLabel={t('datetime_picker.hours')}
							minuteLabel={t('datetime_picker.minutes')}
							periodLabel={t('datetime_picker.period')}
							locale={language}
							invalid={isInvalid}
							value={time}
							onChange={(next) => update(join(day ?? new Date(), next))}
							onBlur={field.onBlur}
						/>
					</div>
				);
			}}
		/>
	);
}
