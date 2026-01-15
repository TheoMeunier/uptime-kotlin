import { Controller, type FieldValues, type Path, type UseFormReturn } from 'react-hook-form';
import type { MultiSelectGroup, MultiSelectOption } from '@/components/atoms/multi-select';
import { MultiSelect } from '@/components/atoms/multi-select';
import { Field, FieldContent, FieldDescription, FieldError, FieldLabel } from '@/components/atoms/field';

interface FormMultiSelectProps<TFieldValues extends FieldValues> {
	form: UseFormReturn<TFieldValues>;
	name: Path<TFieldValues>;
	label: string;
	description?: string;
	placeholder?: string;
	options: MultiSelectOption[] | MultiSelectGroup[] | readonly string[];
	searchable?: boolean;
	hideSelectAll?: boolean;
	maxCount?: number;
	emptyIndicator?: React.ReactNode;
	closeOnSelect?: boolean;
	disabled?: boolean;
}

export default function FormMultiSelect<TFieldValues extends FieldValues>({
	form,
	name,
	label,
	description,
	placeholder = 'Select options',
	options,
	searchable = true,
	hideSelectAll = false,
	maxCount = 3,
	emptyIndicator,
	closeOnSelect = false,
	disabled = false,
}: FormMultiSelectProps<TFieldValues>) {
	const normalizeDash = (v: string) => v.replace(/[–—]/g, '-');

	const normalizedOptions = (() => {
		if (options.length === 0) return [];

		const firstOption = options[0];

		const normalizeOption = (opt: MultiSelectOption | string): MultiSelectOption => {
			if (typeof opt === 'string') {
				const norm = normalizeDash(opt);
				return { label: norm, value: norm };
			}

			return {
				...opt,
				label: normalizeDash(opt.label),
				value: normalizeDash(opt.value),
			};
		};

		if (firstOption && typeof firstOption === 'object' && 'heading' in firstOption) {
			return (options as MultiSelectGroup[]).map((group) => ({
				heading: normalizeDash(group.heading),
				options: group.options.map((o) => normalizeOption(o)),
			}));
		}

		if (firstOption && typeof firstOption === 'object' && 'label' in firstOption && 'value' in firstOption) {
			return (options as MultiSelectOption[]).map((opt) => normalizeOption(opt));
		}

		return (options as readonly string[]).map((opt) => normalizeOption(opt));
	})();

	return (
		<Controller
			name={name}
			control={form.control}
			render={({ field, fieldState }) => (
				<Field>
					<FieldContent>
						<FieldLabel htmlFor={name}>{label}</FieldLabel>
						{description && <FieldDescription>{description}</FieldDescription>}
					</FieldContent>
					<MultiSelect
						options={normalizedOptions}
						onValueChange={field.onChange}
						defaultValue={field.value ?? []}
						placeholder={placeholder}
						searchable={searchable}
						hideSelectAll={hideSelectAll}
						maxCount={maxCount}
						emptyIndicator={emptyIndicator}
						closeOnSelect={closeOnSelect}
						disabled={disabled}
					/>
					<FieldError>{fieldState.error?.message}</FieldError>
				</Field>
			)}
		/>
	);
}

export type { MultiSelectOption, MultiSelectGroup };
