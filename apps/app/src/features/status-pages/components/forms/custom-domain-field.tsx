import { useTranslation } from 'react-i18next';
import { Field, FieldDescription, FieldError, FieldLabel } from '@/components/atoms/field.tsx';
import PrefixedInput from '@/components/molecules/forms/prefixed-input.tsx';
import { cleanDomain } from '@/features/status-pages/lib/editor.ts';

const PREFIX = 'https://';

interface CustomDomainFieldProps {
	value: string;
	onChange: (value: string) => void;
	error?: string;
	showError: boolean;
}

export default function CustomDomainField({ value, onChange, error, showError }: CustomDomainFieldProps) {
	const { t } = useTranslation();
	const visibleError = showError || value.length > 0 ? error : undefined;

	return (
		<Field>
			<FieldLabel htmlFor="custom-domain">{t('status_pages.form.custom_domain')}</FieldLabel>
			<PrefixedInput
				id="custom-domain"
				prefix={PREFIX}
				value={value}
				maxLength={253}
				spellCheck={false}
				autoComplete="off"
				inputMode="url"
				placeholder="status.example.com"
				aria-invalid={Boolean(visibleError)}
				onChange={(event) => onChange(event.target.value)}
				onBlur={() => onChange(cleanDomain(value))}
			/>
			<FieldDescription>
				{t('status_pages.form.custom_domain_description', { host: window.location.hostname })}
			</FieldDescription>
			<FieldError>{visibleError}</FieldError>
		</Field>
	);
}
