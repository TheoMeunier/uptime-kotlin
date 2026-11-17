import { useMutation, useQueryClient } from '@tanstack/react-query';
import backupService, { type RestoreBackupParams } from '@/features/backup/services/backupService.ts';

export default function useRestoreBackup() {
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: async (params: RestoreBackupParams) => backupService.restoreBackup(params),
		onSuccess: async (report) => {
			if (!report.dry_run) await client.invalidateQueries();
		},
	});

	return { restoreBackup: mutation.mutate, isRestoring: mutation.isPending };
}
