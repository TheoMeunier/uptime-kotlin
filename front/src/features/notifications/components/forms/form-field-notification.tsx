import type { FieldConfig } from "@/features/notifications/types/notification.type.ts";
import { Input } from "@/components/atoms/input.tsx";
import {
  Field,
  FieldDescription,
  FieldError,
  FieldLabel,
} from "@/components/atoms/field.tsx";
import FormSwitch from "@/components/molecules/forms/form-switch.tsx";
import FormMultiSelect from "@/components/molecules/forms/form-select-multiple.tsx";

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
          defaultValue={field.default_value}
          min={1}
        />

        {field.description && (
          <FieldDescription>{field.description}</FieldDescription>
        )}

        <FieldError>{form.formState.errors[field.name]?.message}</FieldError>
      </Field>
    );
  }

  if (field.input_type === "switch") {
    return (
      <Field className="mb-2">
        <div className="flex items-center space-x-2">
          <FormSwitch
            form={form}
            name={field.name}
            label={field.label}
            description={field.description}
            defaultValue={!!field.default_value}
          />
        </div>
        <FieldError>{form.formState.errors[field.name]?.message}</FieldError>
      </Field>
    );
  }

  if (field.input_type === "switch_multiple") {
    return (
      <FormMultiSelect
        form={form}
        label={field.label}
        name={field.name}
        options={field.options!}
        searchable={field.searchable}
        closeOnSelect={field.closeOnSelect}
      />
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
        defaultValue={field.default_value}
      />
      {field.description && (
        <FieldDescription>{field.description}</FieldDescription>
      )}
      <FieldError>{form.formState.errors[field.name]?.message}</FieldError>
    </Field>
  );
}
