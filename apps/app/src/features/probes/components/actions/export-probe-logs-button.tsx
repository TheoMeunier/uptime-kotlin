import { Button } from '@/components/atoms/button.tsx';
import probeService from '@/features/probes/services/probeService.ts';
import { Download } from 'lucide-react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

export default function ExportProbeLogsButton({ probeId }: { probeId: string }) {
	const { t } = useTranslation();
	const [isExporting, setIsExporting] = useState(false);

	const exportLogs = async () => {
		setIsExporting(true);
		try {
			const { blob, fileName } = await probeService.exportProbeLogs(probeId);
			const url = URL.createObjectURL(blob);
			const link = document.createElement('a');

			link.href = url;
			link.download = fileName;
			link.click();
			URL.revokeObjectURL(url);
		} finally {
			setIsExporting(false);
		}
	};

	return (
		<Button variant="outline" size="sm" className="h-7 rounded-md text-xs" disabled={isExporting} onClick={exportLogs}>
			<Download className="h-3.5 w-3.5" />
			{isExporting ? t('button.exporting') : t('button.actions.export_csv')}
		</Button>
	);
}
