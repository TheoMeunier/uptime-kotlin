import { z } from 'zod';

export const LOG_RETENTION_MIN_DAYS = 30;
export const LOG_RETENTION_MAX_DAYS = 3650;
export const LOG_RETENTION_PRESETS = [30, 90, 180, 365] as const;

export const LOG_RETENTION_KEEP_FOREVER = 0;

export function isValidRetentionDays(days: number): boolean {
	return Number.isInteger(days) && days >= LOG_RETENTION_MIN_DAYS && days <= LOG_RETENTION_MAX_DAYS;
}

export const LogRetentionSettingsSchema = z.object({
	log_retention_days: z.number().nullable(),
});

export type LogRetentionSettings = z.infer<typeof LogRetentionSettingsSchema>;

export const LogRetentionPreviewSchema = z.object({
	retention_days: z.number().nullable(),
	logs_to_delete: z.number(),
	total_logs: z.number(),
	oldest_log_at: z.string().nullable(),
});

export type LogRetentionPreview = z.infer<typeof LogRetentionPreviewSchema>;
