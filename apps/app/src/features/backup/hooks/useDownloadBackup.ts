import { useMutation } from '@tanstack/react-query';
import backupService from '@/features/backup/services/backupService.ts';
import type { SecretsMode } from '@/features/backup/schemas/backup.schema.ts';
import { downloadBlob } from '@/lib/http.ts';

export default function useDownloadBackup() {
	const mutation = useMutation({
		mutationFn: async (secrets: SecretsMode) => backupService.downloadBackup(secrets),
		onSuccess: ({ blob, fileName }) => downloadBlob(blob, fileName),
	});

	return { downloadBackup: mutation.mutate, isDownloading: mutation.isPending };
}
