import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/atoms/popover.tsx';
import {
	Command,
	CommandEmpty,
	CommandGroup,
	CommandInput,
	CommandItem,
	CommandList,
} from '@/components/atoms/command.tsx';
import ProbeStatusDot from '@/features/probes/components/modules/probe-status-dot.tsx';
import type { EditorProbe } from '@/features/status-pages/lib/editor.ts';

interface AddProbePopoverProps {
	available: EditorProbe[];
	onSelect: (probeId: string) => void;
}

export default function AddProbePopover({ available, onSelect }: AddProbePopoverProps) {
	const { t } = useTranslation();
	const [open, setOpen] = useState(false);

	return (
		<Popover open={open} onOpenChange={setOpen}>
			<PopoverTrigger asChild>
				<Button type="button" variant="ghost" size="sm" className="text-muted-foreground mt-2">
					<Plus className="size-4" />
					{t('status_pages.form.add_probe')}
				</Button>
			</PopoverTrigger>
			<PopoverContent className="w-72 p-0" align="start">
				<Command>
					<CommandInput placeholder={t('status_pages.form.search_probe')} />
					<CommandList>
						<CommandEmpty>{t('status_pages.form.no_probe_left')}</CommandEmpty>
						<CommandGroup>
							{available.map((probe) => (
								<CommandItem
									key={probe.id}
									// The id keeps two probes with the same name apart for cmdk.
									value={`${probe.name} ${probe.id}`}
									onSelect={() => {
										onSelect(probe.id);
										setOpen(false);
									}}
								>
									<ProbeStatusDot status={probe.status} />
									<span className="truncate">{probe.name}</span>
								</CommandItem>
							))}
						</CommandGroup>
					</CommandList>
				</Command>
			</PopoverContent>
		</Popover>
	);
}
