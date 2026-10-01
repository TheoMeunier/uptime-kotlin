import { useTranslation } from 'react-i18next';
import { History } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import {
	Dialog,
	DialogClose,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
} from '@/components/atoms/dialog.tsx';
import type { LogRetentionPreview } from '@/features/settings/schemas/log-retention.schema.ts';
import { formatDateTime } from '@/lib/datetime.ts';

interface RetentionReductionDialogProps {
	preview: LogRetentionPreview | null;
	onConfirm: () => void;
	onCancel: () => void;
	isPending?: boolean;
}

export default function RetentionReductionDialog({
	preview,
	onConfirm,
	onCancel,
	isPending = false,
}: RetentionReductionDialogProps) {
	const { t, i18n } = useTranslation();

	return (
		<Dialog open={preview !== null} onOpenChange={(open) => !open && onCancel()}>
			<DialogContent className="sm:max-w-md">
				<DialogHeader className="mt-2 mb-4 items-center text-center">
					<div className="bg-status-down-bg flex h-12 w-12 items-center justify-center rounded-full">
						<History className="text-status-down-fg h-6 w-6" />
					</div>

					<DialogTitle className="mt-4">{t('retention.confirm.title')}</DialogTitle>

					<DialogDescription className="text-muted-foreground text-center text-sm">
						{t('retention.confirm.description', {
							count: preview?.logs_to_delete ?? 0,
							days: preview?.retention_days ?? 0,
						})}
					</DialogDescription>

					{preview?.oldest_log_at && (
						<p className="text-muted-foreground text-center text-xs">
							{t('retention.confirm.oldest', { date: formatDateTime(preview.oldest_log_at, i18n.language) })}
						</p>
					)}
				</DialogHeader>

				<DialogFooter className="grid grid-cols-2 gap-2">
					<DialogClose asChild>
						<Button variant="outline" className="w-full" disabled={isPending}>
							{t('button.cancel')}
						</Button>
					</DialogClose>

					<Button variant="destructive" className="w-full" onClick={onConfirm} disabled={isPending}>
						{t('retention.confirm.confirm')}
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}
