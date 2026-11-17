import api from '@/api/kyClient.ts';
import { getFileNameFromContentDisposition } from '@/lib/http.ts';
import {
	type RestoreReport,
	RestoreReportSchema,
	type ConflictStrategy,
	type SecretsMode,
} from '@/features/backup/schemas/backup.schema.ts';

const RESTORE_TIMEOUT_MS = 120_000;

export type RestoreBackupParams = {
	file: unknown;
	dryRun: boolean;
	onConflict: ConflictStrategy;
};

const backupService = {
	async downloadBackup(secrets: SecretsMode) {
		const response = await api.get('backup', { searchParams: { secrets } });
		const blob = await response.blob();
		const fileName = getFileNameFromContentDisposition(response.headers.get('Content-Disposition'));

		return { blob, fileName: fileName ?? 'uptime-kotlin-backup.json' };
	},

	async restoreBackup({ file, dryRun, onConflict }: RestoreBackupParams): Promise<RestoreReport> {
		const response = await api
			.post('backup/restore', {
				json: file,
				searchParams: { dry_run: String(dryRun), on_conflict: onConflict },
				timeout: RESTORE_TIMEOUT_MS,
			})
			.json();
		return RestoreReportSchema.parse(response);
	},
};

export default backupService;
