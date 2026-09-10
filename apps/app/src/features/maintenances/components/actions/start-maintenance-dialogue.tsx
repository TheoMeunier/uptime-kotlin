import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Wrench } from 'lucide-react';
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
import { Input } from '@/components/atoms/input.tsx';
import { Field, FieldDescription, FieldLabel } from '@/components/atoms/field.tsx';
import { useStartAdHocMaintenance } from '@/features/maintenances/hooks/useMaintenanceActions.ts';

const PRESETS = [15, 30, 60, 120];

export default function StartMaintenanceDialogue({ probeId }: { probeId: string }) {
	const { t } = useTranslation();
	const [open, setOpen] = useState(false);
	const [minutes, setMinutes] = useState(60);
	const { start, isLoading } = useStartAdHocMaintenance(probeId);

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger asChild>
				<Button variant="outline">
					<Wrench className="mr-2 size-4" />
					{t('maintenances.actions.start')}
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-md">
				<DialogHeader className="items-center text-center">
					<span className="bg-status-maintenance-bg mb-2 flex size-12 items-center justify-center rounded-full">
						<Wrench className="text-status-maintenance-fg size-6" />
					</span>
					<DialogTitle>{t('maintenances.title.start')}</DialogTitle>
					<DialogDescription className="text-center">{t('maintenances.description.start')}</DialogDescription>
				</DialogHeader>

				<div className="mt-2 space-y-4">
					<div className="flex flex-wrap gap-2">
						{PRESETS.map((preset) => (
							<Button
								key={preset}
								type="button"
								variant={minutes === preset ? 'default' : 'outline'}
								size="sm"
								onClick={() => setMinutes(preset)}
							>
								{preset < 60 ? `${preset}m` : `${preset / 60}h`}
							</Button>
						))}
					</div>

					<Field>
						<FieldLabel htmlFor="ad-hoc-minutes">{t('maintenances.form.duration')}</FieldLabel>
						<Input
							id="ad-hoc-minutes"
							type="number"
							min={1}
							value={minutes}
							onChange={(event) => setMinutes(Math.max(1, Number(event.target.value) || 1))}
						/>
						<FieldDescription>{t('maintenances.description.start_hint')}</FieldDescription>
					</Field>
				</div>

				<DialogFooter className="mt-4 grid grid-cols-2 gap-2">
					<DialogClose asChild>
						<Button variant="outline" className="w-full" disabled={isLoading}>
							{t('button.cancel')}
						</Button>
					</DialogClose>
					<Button
						className="w-full"
						disabled={isLoading}
						onClick={() => {
							start({ durationMinutes: minutes });
							setOpen(false);
						}}
					>
						{t('maintenances.actions.start')}
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}
