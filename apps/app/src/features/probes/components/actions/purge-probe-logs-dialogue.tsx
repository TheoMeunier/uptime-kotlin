import { Button } from '@/components/atoms/button.tsx';
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
	DialogTrigger,
} from '@/components/atoms/dialog.tsx';
import { Trash2 } from 'lucide-react';
import { DialogClose } from '@radix-ui/react-dialog';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import probeService from '@/features/probes/services/probeService.ts';
import { toast } from 'sonner';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

export default function PurgeProbeLogsDialogue({ probeId, disabled = false }: { probeId: string; disabled?: boolean }) {
	const [open, setOpen] = useState(false);
	const client = useQueryClient();
	const form = useForm();
	const { t } = useTranslation();

	const mutation = useMutation({
		mutationFn: async () => {
			await probeService.purgeProbeLogs(probeId);
		},
		onSuccess: () => {
			client.invalidateQueries({ queryKey: ['probe', probeId] }).then(() => {
				toast.success(t('monitors.alerts.purge_logs'));
				setOpen(false);
			});
		},
	});

	const onSubmit = () => mutation.mutate();

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger asChild>
				<Button variant="outline" size="sm" className="h-7 rounded-md text-xs" disabled={disabled}>
					<Trash2 className="h-3.5 w-3.5" />
					{t('button.actions.purge_logs')}
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-md">
				<form onSubmit={form.handleSubmit(onSubmit)} noValidate>
					<DialogHeader className="items-center text-center mb-4 mt-2">
						<div className="flex h-12 w-12 items-center justify-center rounded-full bg-red-100">
							<Trash2 className="h-6 w-6 text-red-600" />
						</div>

						<DialogTitle className="mt-4">{t('monitors.title.purge_logs')}?</DialogTitle>

						<DialogDescription className="text-sm text-center text-muted-foreground mb-4">
							{t('monitors.description.purge_logs')}
						</DialogDescription>
					</DialogHeader>

					<DialogFooter className="grid grid-cols-2 gap-2">
						<DialogClose asChild>
							<Button variant="outline" className="w-full" disabled={mutation.isPending}>
								{t('button.cancel')}
							</Button>
						</DialogClose>

						<Button variant="destructive" className="w-full" type="submit" disabled={mutation.isPending}>
							{mutation.isPending ? t('button.purging') : t('button.purge')}
						</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	);
}
