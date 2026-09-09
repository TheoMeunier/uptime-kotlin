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
import { useNavigate } from 'react-router';
import { toast } from 'sonner';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';
import { Input } from '@/components/atoms/input.tsx';
import { Field, FieldDescription, FieldLabel } from '@/components/atoms/field.tsx';

function slugify(value: string) {
	return value
		.normalize('NFD')
		.replace(/[\u0300-\u036f]/g, '')
		.toLowerCase()
		.trim()
		.replace(/[^a-z0-9]+/g, '-')
		.replace(/^-+|-+$/g, '');
}

// A name made only of punctuation slugs down to an empty string; falling back to the raw name
// keeps such a monitor deletable instead of locking the confirmation for good.
function confirmationValue(value: string) {
	return slugify(value) || value.trim();
}

export default function DeleteProbeDialogue({ probeId, probeName }: { probeId: string; probeName: string }) {
	const [open, setOpen] = useState(false);
	const [confirmation, setConfirmation] = useState('');
	const client = useQueryClient();
	const navigate = useNavigate();
	const form = useForm();
	const { t } = useTranslation();

	// Deleting a monitor drops its whole history, so the name has to be typed back: an accidental
	// click on a destructive button in a button group should never be enough. Both sides go through
	// the same slug so a name with spaces, accents or capitals stays typable.
	const expectedSlug = confirmationValue(probeName);
	const isConfirmed = expectedSlug.length > 0 && confirmationValue(confirmation) === expectedSlug;

	const mutation = useMutation({
		mutationFn: async () => {
			await probeService.deleteProbe(probeId);
		},
		onSuccess: () => {
			client.invalidateQueries({ queryKey: ['probes'] }).then(() => {
				toast.success(t('monitors.alerts.remove'));
				navigate('/dashboard');
			});
		},
	});

	const onSubmit = () => {
		if (!isConfirmed) return;

		mutation.mutate();
	};

	return (
		<Dialog
			open={open}
			onOpenChange={(next) => {
				setOpen(next);
				setConfirmation('');
			}}
		>
			<DialogTrigger asChild>
				<Button variant="outline" className="text-status-down-fg hover:bg-status-down-bg hover:text-status-down-fg">
					<Trash2 className="mr-2 h-4 w-4" />
					{t('button.actions.remove')}
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-md">
				<form onSubmit={form.handleSubmit(onSubmit)} noValidate>
					<DialogHeader className="items-center text-center mb-4 mt-2">
						<div className="bg-status-down-bg flex h-12 w-12 items-center justify-center rounded-full">
							<Trash2 className="text-status-down-fg h-6 w-6" />
						</div>

						<DialogTitle className="mt-4">{t('monitors.title.remove')} ?</DialogTitle>

						<DialogDescription className="text-sm text-center text-muted-foreground mb-4">
							{t('monitors.description.remove')} ?
						</DialogDescription>
					</DialogHeader>

					<Field className="mb-4">
						<FieldLabel htmlFor="remove-confirmation">{t('monitors.label.remove_confirmation')}</FieldLabel>

						{/* <code> and not <pre>: FieldDescription renders a <p>, which cannot legally contain a
						    <pre>, and only an inline element stays on the same line as the sentence. */}
						<FieldDescription>
							{t('monitors.description.remove_confirmation')}{' '}
							<code className="bg-muted text-status-down-fg rounded px-1.5 py-0.5 font-mono text-xs break-all select-all">
								{expectedSlug}
							</code>
						</FieldDescription>

						<Input
							id="remove-confirmation"
							value={confirmation}
							onChange={(event) => setConfirmation(event.target.value)}
							placeholder={expectedSlug}
							autoComplete="off"
							autoFocus
						/>
					</Field>

					<DialogFooter className="grid grid-cols-2 gap-2">
						<DialogClose asChild>
							<Button type="button" variant="outline" className="w-full" disabled={mutation.isPending}>
								{t('button.cancel')}
							</Button>
						</DialogClose>

						<Button
							variant="destructive"
							className="w-full"
							type="submit"
							disabled={!isConfirmed || mutation.isPending}
						>
							{mutation.isPending ? t('button.removing') : t('button.remove', { entity: t('entity.monitor') })}
						</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	);
}
