import { Controller } from "react-hook-form";
import { MultiSelect } from "@/components/atoms/multi-select";
import type { MultiSelectOption, MultiSelectGroup } from "@/components/atoms/multi-select";
import { Field, FieldContent, FieldDescription, FieldError, FieldLabel } from "@/components/atoms/field";

interface FormMultiSelectProps {
    form: any;
    name: string;
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

export default function FormMultiSelect({
                                            form,
                                            name,
                                            label,
                                            description,
                                            placeholder = "Select options",
                                            options,
                                            searchable = true,
                                            hideSelectAll = false,
                                            maxCount = 3,
                                            emptyIndicator,
                                            closeOnSelect = false,
                                            disabled = false,
                                        }: FormMultiSelectProps) {
    const normalizedOptions = (() => {
        if (options.length === 0) return [];

        const firstOption = options[0];

        if (typeof firstOption === "object" && firstOption !== null && "heading" in firstOption) {
            return options as MultiSelectGroup[];
        }

        if (typeof firstOption === "object" && firstOption !== null && "label" in firstOption && "value" in firstOption) {
            return options as MultiSelectOption[];
        }

        return (options as readonly string[]).map((option) => ({
            label: option,
            value: option,
        }));
    })();

    return (
        <Controller
            name={name}
            control={form.control}
            render={({ field, fieldState }) => (
                <Field data-invalid={fieldState.invalid}>
                    <FieldContent>
                        <FieldLabel htmlFor={name}>{label}</FieldLabel>
                        {description && (
                            <FieldDescription>{description}</FieldDescription>
                        )}
                        {fieldState.invalid && (
                            <FieldError errors={[fieldState.error]} />
                        )}
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
                        className={fieldState.invalid ? "border-red-500" : ""}
                    />
                </Field>
            )}
        />
    );
}

// Export des types pour utilisation
export type { MultiSelectOption, MultiSelectGroup };