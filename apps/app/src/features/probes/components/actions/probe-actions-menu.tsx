import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import { Copy, MoreHorizontal, Pause, Play, Trash2, Wrench } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
} from '@/components/atoms/dropdown-menu.tsx';
import DeleteProbeDialogue from '@/features/probes/components/actions/delete-probe-dialogue.tsx';
import OnOffMonitorProbeDialogue from '@/features/probes/components/actions/on-off-probe-dialogue.tsx';
import StartMaintenanceDialogue from '@/features/maintenances/components/actions/start-maintenance-dialogue.tsx';

type ProbeDialog = 'maintenance' | 'on-off' | 'remove';

export default function ProbeActionsMenu({
	probeId,
	probeName,
	enabled,
}: {
	probeId: string;
	probeName: string;
	enabled: boolean;
}) {
	const { t } = useTranslation();
	const [dialog, setDialog] = useState<ProbeDialog | null>(null);

	const close = (open: boolean) => {
		if (!open) setDialog(null);
	};

	const PauseIcon = enabled ? Pause : Play;

	return (
		<>
			<DropdownMenu>
				<DropdownMenuTrigger asChild>
					<Button variant="outline" aria-label={t('button.actions.more')}>
						<MoreHorizontal className="size-4" />
					</Button>
				</DropdownMenuTrigger>

				<DropdownMenuContent align="end" className="w-52">
					<DropdownMenuItem onSelect={() => setDialog('maintenance')}>
						<Wrench />
						{t('maintenances.actions.start')}
					</DropdownMenuItem>

					<DropdownMenuItem onSelect={() => setDialog('on-off')}>
						<PauseIcon />
						{t(enabled ? 'button.actions.pause' : 'button.actions.resume')}
					</DropdownMenuItem>

					<DropdownMenuItem asChild>
						<Link to={`/monitors/new?from=${probeId}`}>
							<Copy />
							{t('button.actions.duplicate')}
						</Link>
					</DropdownMenuItem>

					<DropdownMenuSeparator />

					<DropdownMenuItem variant="destructive" onSelect={() => setDialog('remove')}>
						<Trash2 />
						{t('button.actions.remove')}
					</DropdownMenuItem>
				</DropdownMenuContent>
			</DropdownMenu>

			<StartMaintenanceDialogue probeId={probeId} open={dialog === 'maintenance'} onOpenChange={close} />
			<OnOffMonitorProbeDialogue probeId={probeId} enabled={enabled} open={dialog === 'on-off'} onOpenChange={close} />
			<DeleteProbeDialogue probeId={probeId} probeName={probeName} open={dialog === 'remove'} onOpenChange={close} />
		</>
	);
}
