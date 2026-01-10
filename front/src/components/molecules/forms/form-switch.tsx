import { Controller } from 'react-hook-form';
import { Field, FieldContent, FieldDescription, FieldError, FieldLabel } from '@/components/atoms/field.tsx';
import { Switch } from '@/components/atoms/switch.tsx';

interface PropsInterface {
	form: any;
	name: string;
	label: string;
	description?: string;
	defaultValue?: boolean;
}

export default function FormSwitch(props: PropsInterface) {
	return (
		<Controller
			name={props.name}
			control={props.form.control}
			defaultValue={props.defaultValue ?? false}
			render={({ field, fieldState }) => (
				<Field orientation="horizontal" data-invalid={fieldState.invalid}>
					<Switch
						id={props.label}
						name={field.name}
						checked={field.value}
						onCheckedChange={field.onChange}
						data-invalid={fieldState.invalid}
					/>
					<FieldContent>
						<FieldLabel htmlFor="form-rhf-switch-twoFactor">{props.label}</FieldLabel>
						<FieldDescription>{props.description}</FieldDescription>
						{fieldState.invalid && <FieldError errors={[fieldState.error]} />}
					</FieldContent>
				</Field>
			)}
		/>
	);
}
