import { Download } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/atoms/button.tsx';
import { type IncidentFilters, useExportIncidents } from '@/features/incidents/hooks/useIncidents.ts';

export default function ExportIncidentsButton({ filters }: { filters: IncidentFilters }) {
	const { t } = useTranslation();
	const exportIncidents = useExportIncidents();

	return (
		<Button
			variant="outline"
			disabled={exportIncidents.isPending}
			title={t('incidents.export.hint')}
			onClick={() => exportIncidents.mutate(filters)}
		>
			<Download />
			{exportIncidents.isPending ? t('button.exporting') : t('button.actions.export_csv')}
		</Button>
	);
}
