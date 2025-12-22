import type { FieldConfig } from "@/features/notifications/types/notification.type.ts";
import { Input } from "@/components/atoms/input.tsx";
import { Field, FieldError, FieldLabel } from "@/components/atoms/field.tsx";

interface FormFieldNotificationProps {
  key: string;
  field: FieldConfig;
  form: any;
}

export default function FormFieldNotification({
  key,
  field,
  form,
}: FormFieldNotificationProps) {
  if (field.input_type === "number") {
    return (
      <Field key={key}>
        <FieldLabel className="block text-sm font-medium text-gray-700">
          {field.label}
        </FieldLabel>

        <Input
          {...form.register(field.name, { valueAsNumber: true })}
          type="number"
          placeholder={field.placeholder}
          min={1}
        />

        <FieldError>{form.formState.errors[field.name]?.message}</FieldError>
      </Field>
    );
  }

  return (
    <Field key={key}>
      <FieldLabel className="block text-sm font-medium text-gray-700">
        {field.label}
      </FieldLabel>
      <Input
        {...form.register(field.name)}
        type={field.input_type}
        placeholder={field.placeholder}
      />

      <FieldError>{form.formState.errors[field.name]?.message}</FieldError>
    </Field>
  );
}
