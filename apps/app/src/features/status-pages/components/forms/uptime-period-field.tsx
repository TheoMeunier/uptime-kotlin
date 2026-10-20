import { useTranslation } from 'react-i18next';
import { Field, FieldDescription, FieldError, FieldLabel } from '@/components/atoms/field.tsx';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/atoms/select.tsx';
import { isPeriodAllowed, type UptimePeriod, UptimePeriodEnum } from '@/features/status-pages/lib/uptime-period.ts';

interface UptimePeriodFieldProps {
	value: UptimePeriod;
	onChange: (period: UptimePeriod) => void;
	retentionDays: number | null;
}

export default function UptimePeriodField({ value, onChange, retentionDays }: UptimePeriodFieldProps) {
	const { t } = useTranslation();
	const allowed = isPeriodAllowed(value, retentionDays);

	return (
		<Field data-invalid={!allowed}>
			<FieldLabel htmlFor="uptime-period">{t('status_pages.form.uptime_period')}</FieldLabel>
			<Select value={value} onValueChange={(period) => onChange(period as UptimePeriod)}>
				<SelectTrigger id="uptime-period" className="w-full" aria-invalid={!allowed}>
					<SelectValue />
				</SelectTrigger>
				<SelectContent>
					{UptimePeriodEnum.map((period) => {
						const disabled = !isPeriodAllowed(period, retentionDays);

						return (
							<SelectItem key={period} value={period} disabled={disabled}>
								{t(`status_pages.uptime_period.${period}`)}
								{disabled && (
									<span className="text-muted-foreground text-xs">
										{t('status_pages.form.uptime_period_retention', { days: retentionDays })}
									</span>
								)}
							</SelectItem>
						);
					})}
				</SelectContent>
			</Select>
			<FieldDescription>
				{retentionDays == null
					? t('status_pages.form.uptime_period_description')
					: t('status_pages.form.uptime_period_description_limited', { days: retentionDays })}
			</FieldDescription>
			{!allowed && <FieldError>{t('status_pages.form.uptime_period_too_long', { days: retentionDays })}</FieldError>}
		</Field>
	);
}
