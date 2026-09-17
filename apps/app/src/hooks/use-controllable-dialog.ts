import { useState } from 'react';

export type ControllableDialogProps = {
	open?: boolean;
	onOpenChange?: (open: boolean) => void;
};

export default function useControllableDialog({ open, onOpenChange }: ControllableDialogProps) {
	const [internalOpen, setInternalOpen] = useState(false);
	const isControlled = open !== undefined;

	return {
		isControlled,
		open: isControlled ? open : internalOpen,
		setOpen: (next: boolean) => {
			if (!isControlled) setInternalOpen(next);
			onOpenChange?.(next);
		},
	};
}
