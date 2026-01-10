import { Controller, type FieldValues, type Path, type PathValue, type UseFormReturn } from 'react-hook-form';
import { Field, FieldContent, FieldDescription, FieldError, FieldLabel } from '@/components/atoms/field.tsx';
import { Switch } from '@/components/atoms/switch.tsx';

interface FormSwitchProps<TFieldValues extends FieldValues> {
	form: UseFormReturn<TFieldValues>;
	name: Path<TFieldValues>;
	label: string;
	description?: string;
	defaultValue?: boolean;
}

export default function FormSwitch<TFieldValues extends FieldValues>({
	form,
	name,
	label,
	description,
	defaultValue = false,
}: FormSwitchProps<TFieldValues>) {
	return (
		<Controller
			name={name}
			control={form.control}
			defaultValue={defaultValue as PathValue<TFieldValues, Path<TFieldValues>>}
			render={({ field, fieldState }) => (
				<Field orientation="horizontal" data-invalid={fieldState.invalid}>
					<Switch
						id={name}
						name={field.name}
						checked={field.value}
						onCheckedChange={field.onChange}
						data-invalid={fieldState.invalid}
					/>
					<FieldContent>
						<FieldLabel htmlFor={name}>{label}</FieldLabel>
						{description && <FieldDescription>{description}</FieldDescription>}
						{fieldState.invalid && <FieldError errors={[fieldState.error]} />}
					</FieldContent>
				</Field>
			)}
		/>
	);
}
