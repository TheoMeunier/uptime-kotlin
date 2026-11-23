import { type FormEvent, type ReactNode, useState } from 'react';
import { useTranslation } from 'react-i18next';
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
import { FieldGroup } from '@/components/atoms/field.tsx';

interface FormDialogueProps {
	trigger: ReactNode;
	title: string;
	description: ReactNode;
	children: ReactNode;
	submitLabel: string;
	pendingLabel?: string;
	onSubmit: () => void;
	isPending?: boolean;
	canSubmit?: boolean;
	destructive?: boolean;
	result?: ReactNode;
	resultDescription?: ReactNode;
	closeLabel?: string;
	onClose?: () => void;
	open?: boolean;
	onOpenChange?: (open: boolean) => void;
}

export default function FormDialogue({
	trigger,
	title,
	description,
	children,
	submitLabel,
	pendingLabel,
	onSubmit,
	isPending = false,
	canSubmit = true,
	destructive = false,
	result,
	resultDescription,
	closeLabel,
	onClose,
	open: controlledOpen,
	onOpenChange,
}: FormDialogueProps) {
	const { t } = useTranslation();
	const [internalOpen, setInternalOpen] = useState(false);
	const open = controlledOpen ?? internalOpen;

	const setOpen = (next: boolean) => {
		if (controlledOpen === undefined) setInternalOpen(next);
		onOpenChange?.(next);
		if (!next) onClose?.();
	};

	const submit = (event: FormEvent) => {
		event.preventDefault();
		if (canSubmit && !isPending) onSubmit();
	};

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger asChild>{trigger}</DialogTrigger>
			<DialogContent
				className="sm:max-w-md"
				onInteractOutside={(event) => {
					if (result) event.preventDefault();
				}}
			>
				<DialogHeader>
					<DialogTitle>{title}</DialogTitle>
					<DialogDescription>{result && resultDescription ? resultDescription : description}</DialogDescription>
				</DialogHeader>

				{result ? (
					<>
						{result}
						<DialogFooter className="mt-2">
							<DialogClose asChild>
								<Button className="w-full">{closeLabel ?? t('button.close')}</Button>
							</DialogClose>
						</DialogFooter>
					</>
				) : (
					<form onSubmit={submit}>
						<FieldGroup className="my-4">{children}</FieldGroup>
						<DialogFooter className="grid grid-cols-2 gap-2">
							<DialogClose asChild>
								<Button type="button" variant="outline" disabled={isPending}>
									{t('button.cancel')}
								</Button>
							</DialogClose>
							<Button
								type="submit"
								variant={destructive ? 'destructive' : 'default'}
								disabled={isPending || !canSubmit}
							>
								{isPending && pendingLabel ? pendingLabel : submitLabel}
							</Button>
						</DialogFooter>
					</form>
				)}
			</DialogContent>
		</Dialog>
	);
}
