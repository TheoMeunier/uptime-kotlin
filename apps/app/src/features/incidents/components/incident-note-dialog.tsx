import { type FormEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { NotebookPen } from 'lucide-react';
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
import { Textarea } from '@/components/atoms/textarea.tsx';
import type { Incident } from '@/features/incidents/schemas/incident.schema.ts';
import { useUpdateIncidentNote } from '@/features/incidents/hooks/useIncidents.ts';
import { formatDateTime } from '@/lib/datetime.ts';

const NOTE_MAX_LENGTH = 2000;

export default function IncidentNoteDialog({ incident }: { incident: Incident }) {
	const { t, i18n } = useTranslation();
	const [open, setOpen] = useState(false);
	const [note, setNote] = useState(incident.note ?? '');
	const mutation = useUpdateIncidentNote();
	const label = incident.note ? t('incidents.note.edit') : t('incidents.note.add');

	const onOpenChange = (next: boolean) => {
		if (next) setNote(incident.note ?? '');
		setOpen(next);
	};

	const onSubmit = (event: FormEvent<HTMLFormElement>) => {
		event.preventDefault();
		mutation.mutate({ incidentId: incident.id, note }, { onSuccess: () => setOpen(false) });
	};

	return (
		<Dialog open={open} onOpenChange={onOpenChange}>
			<DialogTrigger asChild>
				<Button
					variant="ghost"
					size="icon-sm"
					className="text-muted-foreground size-7 shrink-0"
					aria-label={label}
					title={label}
				>
					<NotebookPen className="size-3.5" />
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-lg">
				<form onSubmit={onSubmit} className="space-y-4">
					<DialogHeader>
						<DialogTitle>{t('incidents.note.title')}</DialogTitle>
						<DialogDescription>
							{t('incidents.note.description', {
								probe: incident.probe_name,
								time: formatDateTime(incident.started_at, i18n.language),
							})}
						</DialogDescription>
					</DialogHeader>

					<div className="space-y-1.5">
						<Textarea
							value={note}
							onChange={(event) => setNote(event.target.value)}
							maxLength={NOTE_MAX_LENGTH}
							rows={5}
							placeholder={t('incidents.note.placeholder')}
							aria-label={t('incidents.note.title')}
							autoFocus
						/>
						<p className="text-muted-foreground tabular text-right text-xs">
							{t('incidents.note.length', { length: note.length, max: NOTE_MAX_LENGTH })}
						</p>
					</div>

					<DialogFooter>
						<DialogClose asChild>
							<Button type="button" variant="outline" disabled={mutation.isPending}>
								{t('button.cancel')}
							</Button>
						</DialogClose>
						<Button type="submit" disabled={mutation.isPending}>
							{mutation.isPending ? t('button.saving') : t('incidents.note.save')}
						</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	);
}
