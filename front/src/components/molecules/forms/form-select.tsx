import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/atoms/select.tsx";
import {Controller} from "react-hook-form";

interface SelectOption {
    value: string;
    label: string;
}

interface FormSelectProps {
    form: any;
    name: string;
    placeholder?: string;
    options: SelectOption[] | readonly string[];
}

export default function FormSelect({form, name, placeholder = "Select an option", options}: FormSelectProps) {
    // Normalise les options pour supporter les deux formats
    const normalizedOptions = options.map(option =>
        typeof option === 'string'
            ? {value: option, label: option}
            : option
    );

    return <Controller
        name={name}
        control={form.control}
        render={({field, fieldState}) => (
            <Select
                onValueChange={field.onChange}
                value={field.value}
                defaultValue={field.value}
            >
                <SelectTrigger className="w-[180px]" data-invalid={fieldState.invalid}>
                    <SelectValue placeholder={placeholder}/>
                </SelectTrigger>
                <SelectContent>
                    {normalizedOptions.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                            {option.label}
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
        )}
    />
}