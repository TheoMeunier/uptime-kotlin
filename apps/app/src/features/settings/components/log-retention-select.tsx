import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Input } from '@/components/atoms/input.tsx';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/atoms/select.tsx';
import {
	LOG_RETENTION_KEEP_FOREVER,
	LOG_RETENTION_MAX_DAYS,
	LOG_RETENTION_MIN_DAYS,
	LOG_RETENTION_PRESETS,
} from '@/features/settings/schemas/log-retention.schema.ts';

const INHERIT = 'inherit';
const FOREVER = 'forever';
const CUSTOM = 'custom';

interface LogRetentionSelectProps {
	id: string;
	value: number | null | undefined;
	onChange: (value: number | null) => void;
	allowInherit?: boolean;
	inheritedDays?: number | null;
	disabled?: boolean;
}

export default function LogRetentionSelect({
	id,
	value,
	onChange,
	allowInherit = false,
	inheritedDays = null,
	disabled = false,
}: LogRetentionSelectProps) {
	const { t } = useTranslation();
	const foreverValue = allowInherit ? LOG_RETENTION_KEEP_FOREVER : null;
	const [customRequested, setCustomRequested] = useState(false);

	const selected = (() => {
		if (allowInherit && (value === null || value === undefined)) return INHERIT;
		if (value === foreverValue || value === null || value === undefined) return FOREVER;
		if (!customRequested && (LOG_RETENTION_PRESETS as readonly number[]).includes(value)) return String(value);
		return CUSTOM;
	})();

	const daysLabel = (days: number | null) =>
		days === null ? t('retention.option.forever') : t('retention.option.days', { count: days });

	const handleSelect = (key: string) => {
		setCustomRequested(key === CUSTOM);

		if (key === INHERIT) onChange(null);
		else if (key === FOREVER) onChange(foreverValue);
		else if (key === CUSTOM) onChange(typeof value === 'number' && value > 0 ? value : LOG_RETENTION_MIN_DAYS);
		else onChange(Number(key));
	};

	return (
		<div className="flex flex-col gap-2">
			<Select value={selected} onValueChange={handleSelect} disabled={disabled}>
				<SelectTrigger id={id} className="w-full">
					<SelectValue />
				</SelectTrigger>

				<SelectContent>
					{allowInherit && (
						<SelectItem value={INHERIT}>
							{t('retention.option.inherit', { value: daysLabel(inheritedDays) })}
						</SelectItem>
					)}
					{LOG_RETENTION_PRESETS.map((days) => (
						<SelectItem key={days} value={String(days)}>
							{daysLabel(days)}
						</SelectItem>
					))}
					<SelectItem value={FOREVER}>{t('retention.option.forever')}</SelectItem>
					<SelectItem value={CUSTOM}>{t('retention.option.custom')}</SelectItem>
				</SelectContent>
			</Select>

			{selected === CUSTOM && (
				<Input
					id={`${id}-custom`}
					type="number"
					aria-label={t('retention.label.custom_days')}
					min={LOG_RETENTION_MIN_DAYS}
					max={LOG_RETENTION_MAX_DAYS}
					step={1}
					value={typeof value === 'number' ? value : ''}
					disabled={disabled}
					onChange={(event) => {
						const days = Number.parseInt(event.target.value, 10);
						if (!Number.isNaN(days)) onChange(days);
					}}
				/>
			)}
		</div>
	);
}
