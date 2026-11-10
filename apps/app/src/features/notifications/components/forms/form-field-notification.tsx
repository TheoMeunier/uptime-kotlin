import type { FieldConfig } from '@/features/notifications/types/notification.type.ts';
import { Input } from '@/components/atoms/input.tsx';
import { Field, FieldDescription, FieldError, FieldLabel } from '@/components/atoms/field.tsx';
import { Textarea } from '@/components/atoms/textarea.tsx';
import FormSwitch from '@/components/molecules/forms/form-switch.tsx';
import FormMultiSelect from '@/components/molecules/forms/form-select-multiple.tsx';
import FormSelect from '@/components/molecules/forms/form-select.tsx';
import { useTranslation } from 'react-i18next';
import { ExternalLink } from 'lucide-react';
import FormWebPushSubscription from '@/features/notifications/components/forms/form-web-push-subscription.tsx';

interface FormFieldNotificationProps {
	field: FieldConfig;
	form: any;
}

export default function FormFieldNotification({ field, form }: FormFieldNotificationProps) {
	if (field.input_type === 'textarea') {
		return (
			<Field>
				<FieldLabel className="block text-sm font-medium text-foreground">{field.label}</FieldLabel>
				<Textarea
					{...form.register(field.name)}
					placeholder={field.placeholder}
					defaultValue={field.default_value}
					rows={5}
				/>
				<FieldHelp field={field} />
				<FieldError>{form.formState.errors[field.name]?.message}</FieldError>
			</Field>
		);
	}

	if (field.input_type === 'number') {
		return (
			<Field>
				<FieldLabel className="block text-sm font-medium text-foreground">{field.label}</FieldLabel>

				<Input
					{...form.register(field.name, {
						setValueAs: (value: string) => (value === '' || value === null ? undefined : Number(value)),
					})}
					type="number"
					placeholder={field.placeholder}
					defaultValue={field.default_value}
					min={field.min ?? 1}
					max={field.max}
				/>

				<FieldHelp field={field} />

				<FieldError>{form.formState.errors[field.name]?.message}</FieldError>
			</Field>
		);
	}

	if (field.input_type === 'switch') {
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

	if (field.input_type === 'switch_multiple') {
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

	if (field.input_type === 'web_push') {
		return <FormWebPushSubscription field={field} form={form} />;
	}

	if (field.input_type === 'select') {
		return (
			<Field>
				<FieldLabel className="block text-sm font-medium text-foreground">{field.label}</FieldLabel>
				<FormSelect form={form} name={field.name} options={field.options as string[]} />
				<FieldHelp field={field} />
				<FieldError>{form.formState.errors[field.name]?.message}</FieldError>
			</Field>
		);
	}
	return (
		<Field>
			<FieldLabel className="block text-sm font-medium text-foreground">{field.label}</FieldLabel>
			<Input
				{...form.register(field.name)}
				type={field.input_type}
				placeholder={field.placeholder}
				defaultValue={field.default_value}
				autoComplete={field.input_type === 'password' ? 'new-password' : undefined}
			/>
			<FieldHelp field={field} />
			<FieldError>{form.formState.errors[field.name]?.message}</FieldError>
		</Field>
	);
}

function FieldHelp({ field }: { field: FieldConfig }) {
	const { t } = useTranslation();
	if (!field.description && !field.docs_url) return null;

	return (
		<FieldDescription>
			{field.description}
			{field.description && field.docs_url && ' '}
			{field.docs_url && (
				<a
					href={field.docs_url}
					target="_blank"
					rel="noopener noreferrer"
					className="inline-flex items-center gap-1 underline underline-offset-4"
				>
					{t('form.label.see_documentation')}
					<ExternalLink className="size-3" aria-hidden />
					<span className="sr-only">{t('layout.sidebar.new_tab')}</span>
				</a>
			)}
		</FieldDescription>
	);
}
