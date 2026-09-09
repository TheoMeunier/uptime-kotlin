import {
	Select,
	SelectContent,
	SelectGroup,
	SelectItem,
	SelectLabel,
	SelectSeparator,
	SelectTrigger,
	SelectValue,
} from '@/components/atoms/select.tsx';
import { Controller, type FieldValues, type Path, type UseFormReturn } from 'react-hook-form';

interface SelectOption {
	value: string;
	label: string;
}

interface SelectOptionGroup {
	label: string;
	options: SelectOption[] | readonly string[];
}

type SelectOptions = SelectOption[] | readonly string[] | SelectOptionGroup[];

interface FormSelectProps<TFieldValues extends FieldValues> {
	form: UseFormReturn<TFieldValues>;
	name: Path<TFieldValues>;
	placeholder?: string;
	/** A flat list, or groups rendered under their own heading. */
	options: SelectOptions;
	onValueChange?: (value: string) => void;
	className?: string;
}

const normalise = (options: SelectOption[] | readonly string[]): SelectOption[] =>
	options.map((option) => (typeof option === 'string' ? { value: option, label: option } : option));

const isGrouped = (options: SelectOptions): options is SelectOptionGroup[] =>
	options.length > 0 && typeof options[0] === 'object' && options[0] !== null && 'options' in options[0];

export default function FormSelect<TFieldValues extends FieldValues>({
	form,
	name,
	placeholder = 'Select an option',
	options,
	onValueChange,
	className = 'w-full',
}: FormSelectProps<TFieldValues>) {
	const grouped = isGrouped(options);

	return (
		<Controller
			name={name}
			control={form.control}
			render={({ field, fieldState }) => (
				<Select
					onValueChange={(value) => {
						field.onChange(value);
						onValueChange?.(value);
					}}
					value={field.value}
				>
					<SelectTrigger className={className} data-invalid={fieldState.invalid}>
						<SelectValue placeholder={placeholder} />
					</SelectTrigger>
					<SelectContent>
						{grouped
							? options.map((group, index) => (
									<SelectGroup key={group.label}>
										{index > 0 && <SelectSeparator />}
										<SelectLabel>{group.label}</SelectLabel>
										{normalise(group.options).map((option) => (
											<SelectItem key={option.value} value={option.value}>
												{option.label}
											</SelectItem>
										))}
									</SelectGroup>
								))
							: normalise(options as SelectOption[] | readonly string[]).map((option) => (
									<SelectItem key={option.value} value={option.value}>
										{option.label}
									</SelectItem>
								))}
					</SelectContent>
				</Select>
			)}
		/>
	);
}

export type { SelectOption, SelectOptionGroup };
