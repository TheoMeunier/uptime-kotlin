import { useEffect, useState } from 'react';
import { Controller, type UseFormReturn } from 'react-hook-form';
import {
	Field,
	FieldDescription,
	FieldError,
	FieldGroup,
	FieldLabel,
	FieldLegend,
	FieldSet,
} from '@/components/atoms/field.tsx';
import { Input } from '@/components/atoms/input.tsx';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/atoms/select.tsx';
import { Textarea } from '@/components/atoms/textarea.tsx';
import type { StoreProbeSchema } from '@/features/probes/hooks/useProbeForm.ts';
import { useTranslation } from 'react-i18next';

interface HttpAdvancedFieldsFormProps {
	form: UseFormReturn<StoreProbeSchema>;
}

export default function HttpAdvancedFieldsForm({ form }: HttpAdvancedFieldsFormProps) {
	const { t } = useTranslation();
	const authentication = form.watch('authentication');
	const authenticationType = authentication?.type;

	return (
		<FieldSet>
			<FieldLegend>{t('monitors.title.http_request_assertions')}</FieldLegend>
			<FieldGroup className="mt-4">
				<Field>
					<FieldLabel>{t('monitors.label.request_body')}</FieldLabel>
					<Textarea {...form.register('body')} rows={4} placeholder={'{"health":"check"}'} />
				</Field>
				<JsonField
					form={form}
					name="headers"
					label={t('monitors.label.request_headers_json')}
					placeholder={'{"X-Api-Key":"value"}'}
					fallback={{}}
				/>
				<Controller
					name="authentication"
					control={form.control}
					render={({ field, fieldState }) => (
						<Field data-invalid={fieldState.invalid}>
							<FieldLabel htmlFor="http-authentication">{t('monitors.label.authentication')}</FieldLabel>
							<Select
								value={field.value?.type ?? 'NONE'}
								onValueChange={(type) => {
									field.onChange(type === 'NONE' ? null : { type });
								}}
							>
								<SelectTrigger id="http-authentication" className="w-full">
									<SelectValue />
								</SelectTrigger>
								<SelectContent>
									<SelectItem value="NONE">{t('monitors.option.no_authentication')}</SelectItem>
									<SelectItem value="BASIC">{t('monitors.option.basic_authentication')}</SelectItem>
									<SelectItem value="BEARER">{t('monitors.option.bearer_token')}</SelectItem>
								</SelectContent>
							</Select>
							<FieldDescription>{t('monitors.description.authentication_optional')}</FieldDescription>
							{fieldState.error && <FieldError errors={[fieldState.error]} />}
						</Field>
					)}
				/>
				{authenticationType === 'BASIC' && (
					<>
						<Input {...form.register('authentication.username')} placeholder={t('monitors.placeholder.username')} />
						<Input
							{...form.register('authentication.password')}
							type="password"
							placeholder={t('monitors.placeholder.password')}
						/>
					</>
				)}
				{authenticationType === 'BEARER' && (
					<Input
						{...form.register('authentication.token')}
						type="password"
						placeholder={t('monitors.placeholder.bearer_token')}
					/>
				)}
				<JsonField
					form={form}
					name="assertions"
					label={t('monitors.label.assertions_json')}
					placeholder={'[{"type":"JSON_EQUALS","path":"status","expected":"ok"}]'}
					fallback={[]}
				/>
				<JsonField
					form={form}
					name="steps"
					label={t('monitors.label.scenario_steps_json')}
					placeholder={
						'[{"name":"Login","url":"https://example.test/login","method":"POST","http_code_allowed":["200"]}]'
					}
					fallback={[]}
				/>
			</FieldGroup>
		</FieldSet>
	);
}

interface JsonFieldProps {
	form: UseFormReturn<StoreProbeSchema>;
	name: 'headers' | 'assertions' | 'steps';
	label: string;
	placeholder: string;
	fallback: Record<string, string> | unknown[];
}

function JsonField({ form, name, label, placeholder, fallback }: JsonFieldProps) {
	const { t } = useTranslation();
	const [value, setValue] = useState('');
	const [error, setError] = useState<string>();

	useEffect(() => {
		setValue(JSON.stringify(form.getValues(name) ?? fallback, null, 2));
	}, [form, name]);

	return (
		<Field>
			<FieldLabel>{label}</FieldLabel>
			<Textarea
				value={value}
				onChange={(event) => {
					const next = event.target.value;
					setValue(next);
					try {
						form.setValue(name, JSON.parse(next), { shouldValidate: true });
						form.clearErrors(name);
						setError(undefined);
					} catch {
						const message = t('monitors.error.invalid_json');
						form.setError(name, { type: 'validate', message });
						setError(message);
					}
				}}
				rows={6}
				placeholder={placeholder}
			/>
			<FieldError>{error}</FieldError>
		</Field>
	);
}
