import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { LucideIcon } from 'lucide-react';
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

interface ConfirmDialogueProps {
	trigger: React.ReactNode;
	icon: LucideIcon;
	title: string;
	description: React.ReactNode;
	confirmLabel: string;
	onConfirm: () => void;
	isPending?: boolean;
}

/* Irreversible actions only: the confirm button is always the destructive one. */
export default function ConfirmDialogue({
	trigger,
	icon: Icon,
	title,
	description,
	confirmLabel,
	onConfirm,
	isPending = false,
}: ConfirmDialogueProps) {
	const { t } = useTranslation();
	const [open, setOpen] = useState(false);

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger asChild>{trigger}</DialogTrigger>

			<DialogContent className="sm:max-w-md">
				<DialogHeader className="items-center text-center">
					<span className="bg-status-down-bg mb-2 flex size-12 items-center justify-center rounded-full">
						<Icon className="text-status-down-fg size-6" />
					</span>
					<DialogTitle>{title}</DialogTitle>
					<DialogDescription className="text-center">{description}</DialogDescription>
				</DialogHeader>

				<DialogFooter className="mt-4 grid grid-cols-2 gap-2">
					<DialogClose asChild>
						<Button variant="outline" className="w-full" disabled={isPending}>
							{t('button.cancel')}
						</Button>
					</DialogClose>
					<Button
						variant="destructive"
						className="w-full"
						disabled={isPending}
						onClick={() => {
							onConfirm();
							setOpen(false);
						}}
					>
						{confirmLabel}
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}
