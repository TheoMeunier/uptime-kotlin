import { useTranslation } from 'react-i18next';
import { Field, FieldDescription, FieldLabel } from '@/components/atoms/field.tsx';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/atoms/select.tsx';
import { type UptimePeriod, UptimePeriodEnum } from '@/features/status-pages/lib/uptime-period.ts';

interface UptimePeriodFieldProps {
	value: UptimePeriod;
	onChange: (period: UptimePeriod) => void;
}

export default function UptimePeriodField({ value, onChange }: UptimePeriodFieldProps) {
	const { t } = useTranslation();

	return (
		<Field>
			<FieldLabel htmlFor="uptime-period">{t('status_pages.form.uptime_period')}</FieldLabel>
			<Select value={value} onValueChange={(period) => onChange(period as UptimePeriod)}>
				<SelectTrigger id="uptime-period" className="w-full">
					<SelectValue />
				</SelectTrigger>
				<SelectContent>
					{UptimePeriodEnum.map((period) => (
						<SelectItem key={period} value={period}>
							{t(`status_pages.uptime_period.${period}`)}
						</SelectItem>
					))}
				</SelectContent>
			</Select>
			<FieldDescription>{t('status_pages.form.uptime_period_description')}</FieldDescription>
		</Field>
	);
}
