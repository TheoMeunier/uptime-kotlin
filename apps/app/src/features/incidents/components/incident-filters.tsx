import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import {
	Select,
	SelectContent,
	SelectGroup,
	SelectItem,
	SelectLabel,
	SelectSeparator,
	SelectTrigger,
	SelectValue,
} from '@/components/atoms/select.tsx';
import probeService from '@/features/probes/services/probeService.ts';
import {
	IncidentPeriods,
	type IncidentPeriod,
	isMonthPeriod,
	monthStart,
	recentMonths,
} from '@/features/incidents/schemas/incident.schema.ts';
import type { IncidentFilters as Filters } from '@/features/incidents/hooks/useIncidents.ts';
import { formatMonthYear } from '@/lib/datetime.ts';

const ALL_PROBES = 'all';
const MONTHS_LISTED = 12;

export default function IncidentFilters({
	filters,
	onChange,
}: {
	filters: Filters;
	onChange: (filters: Filters) => void;
}) {
	const { t, i18n } = useTranslation();
	const { data: probes } = useQuery({ queryKey: ['probes'], queryFn: () => probeService.getProbes() });

	const months = recentMonths(MONTHS_LISTED);
	// A month older than the list, opened from a shared link, still shows in the trigger.
	if (isMonthPeriod(filters.period) && !months.includes(filters.period)) months.push(filters.period);

	return (
		<div className="flex flex-wrap gap-2">
			<Select
				value={filters.probeId ?? ALL_PROBES}
				onValueChange={(value) => onChange({ ...filters, probeId: value === ALL_PROBES ? undefined : value })}
			>
				<SelectTrigger className="w-52" aria-label={t('incidents.filters.probe')}>
					<SelectValue />
				</SelectTrigger>
				<SelectContent>
					<SelectItem value={ALL_PROBES}>{t('incidents.filters.all_probes')}</SelectItem>
					{probes?.map((probe) => (
						<SelectItem key={probe.id} value={probe.id}>
							{probe.name}
						</SelectItem>
					))}
				</SelectContent>
			</Select>

			<Select
				value={filters.period}
				onValueChange={(value) => onChange({ ...filters, period: value as IncidentPeriod })}
			>
				<SelectTrigger className="w-52" aria-label={t('incidents.filters.period')}>
					<SelectValue />
				</SelectTrigger>
				<SelectContent>
					{IncidentPeriods.map((period) => (
						<SelectItem key={period} value={period}>
							{t(`incidents.filters.periods.${period}`)}
						</SelectItem>
					))}
					<SelectSeparator />
					<SelectGroup>
						<SelectLabel>{t('incidents.filters.months')}</SelectLabel>
						{months.map((month, index) => (
							<SelectItem key={month} value={month}>
								{index === 0 ? t('incidents.filters.this_month') : formatMonthYear(monthStart(month), i18n.language)}
							</SelectItem>
						))}
					</SelectGroup>
				</SelectContent>
			</Select>
		</div>
	);
}
