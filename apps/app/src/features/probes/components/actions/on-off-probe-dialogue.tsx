import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Pause, Play } from 'lucide-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { Button } from '@/components/atoms/button.tsx';
import {
	Dialog,
	DialogClose,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
	DialogTrigger,
} from '@/components/atoms/dialog.tsx';
import probeService from '@/features/probes/services/probeService.ts';

export default function OnOffMonitorProbeDialogue({ probeId, enabled }: { probeId: string; enabled: boolean }) {
	const { t } = useTranslation();
	const client = useQueryClient();
	const [open, setOpen] = useState(false);

	const mutation = useMutation({
		mutationFn: async () => {
			await probeService.onoffline(probeId, !enabled);
		},
		onSuccess: async () => {
			await client.invalidateQueries({ queryKey: ['probes'] });
			await client.invalidateQueries({ queryKey: ['probe', probeId] });
			toast.success(t(enabled ? 'monitors.alerts.paused' : 'monitors.alerts.resumed'));
			setOpen(false);
		},
	});

	/*
	 * Pausing is not a failure, so the icon is amber rather than red; resuming is a return to
	 * health, so it is green. The confirm button stays neutral when pausing: in this app green
	 * means "service up", and a green button on the action that stops watching reads backwards.
	 */
	const Icon = enabled ? Pause : Play;
	const iconTone = enabled ? 'bg-status-degraded-bg text-status-degraded-fg' : 'bg-status-up-bg text-status-up-fg';

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger asChild>
				<Button variant="outline">
					<Icon className="mr-2 h-4 w-4" />
					{t(enabled ? 'button.actions.pause' : 'button.actions.resume')}
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-md">
				<DialogHeader className="items-center text-center">
					<span className={`mb-2 flex size-12 items-center justify-center rounded-full ${iconTone}`}>
						<Icon className="size-6" />
					</span>

					<DialogTitle>{t(enabled ? 'monitors.title.pause' : 'monitors.title.resume')}</DialogTitle>
					<DialogDescription className="text-center">
						{t(enabled ? 'monitors.description.pause' : 'monitors.description.resume')}
					</DialogDescription>
				</DialogHeader>

				<DialogFooter className="mt-4 grid grid-cols-2 gap-2">
					<DialogClose asChild>
						<Button variant="outline" className="w-full" disabled={mutation.isPending}>
							{t('button.cancel')}
						</Button>
					</DialogClose>

					<Button className="w-full" onClick={() => mutation.mutate()} disabled={mutation.isPending}>
						{t(enabled ? 'button.actions.pause' : 'button.actions.resume')}
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}
