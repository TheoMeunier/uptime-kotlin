import { useTranslation } from 'react-i18next';
import { Field, FieldContent, FieldDescription, FieldLabel } from '@/components/atoms/field.tsx';
import { Switch } from '@/components/atoms/switch.tsx';

interface PrivateFieldProps {
	value: boolean;
	onChange: (isPrivate: boolean) => void;
}

export default function PrivateField({ value, onChange }: PrivateFieldProps) {
	const { t } = useTranslation();

	return (
		<Field orientation="horizontal">
			<Switch id="is-private" checked={value} onCheckedChange={onChange} />
			<FieldContent>
				<FieldLabel htmlFor="is-private">{t('status_pages.form.private')}</FieldLabel>
				<FieldDescription>{t('status_pages.form.private_description')}</FieldDescription>
			</FieldContent>
		</Field>
	);
}
