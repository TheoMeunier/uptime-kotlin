import { useTranslation } from 'react-i18next';
import { Field, FieldDescription, FieldError, FieldLabel } from '@/components/atoms/field.tsx';
import PrefixedInput from '@/components/molecules/forms/prefixed-input.tsx';

const PREFIX = '/status/';

interface SlugFieldProps {
	value: string;
	onChange: (value: string) => void;
	error?: string;
	showError: boolean;
}

/* The public address of the page, with the full URL previewed as it is typed. */
export default function SlugField({ value, onChange, error, showError }: SlugFieldProps) {
	const { t } = useTranslation();
	const visibleError = showError || value.length > 0 ? error : undefined;

	return (
		<Field>
			<FieldLabel htmlFor="slug">{t('status_pages.form.slug')}</FieldLabel>
			<PrefixedInput
				id="slug"
				prefix={PREFIX}
				value={value}
				maxLength={64}
				spellCheck={false}
				autoComplete="off"
				placeholder="mon-entreprise"
				aria-invalid={Boolean(visibleError)}
				onChange={(event) => onChange(event.target.value)}
			/>
			<FieldDescription className="truncate">
				{window.location.origin}
				{PREFIX}
				{value || '…'}
			</FieldDescription>
			<FieldError>{visibleError}</FieldError>
		</Field>
	);
}
