import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { KeyRound } from 'lucide-react';
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
import CopyableCode from '@/components/molecules/copyable-code.tsx';
import { useResetUserPassword } from '@/features/users/hooks/useUserActions.ts';
import type { User } from '@/features/users/schemas/user.schema.ts';

export default function ResetPasswordDialogue({ user, disabled }: { user: User; disabled: boolean }) {
	const { t } = useTranslation();
	const [open, setOpen] = useState(false);
	const { reset, temporaryPassword, clear, isLoading } = useResetUserPassword();

	const onOpenChange = (next: boolean) => {
		setOpen(next);
		if (!next) clear();
	};

	return (
		<Dialog open={open} onOpenChange={onOpenChange}>
			<DialogTrigger asChild>
				<Button
					variant="outline"
					size="sm"
					disabled={disabled}
					aria-label={t('users.actions.reset_password')}
					title={t('users.actions.reset_password')}
				>
					<KeyRound className="size-4" />
				</Button>
			</DialogTrigger>

			<DialogContent
				className="sm:max-w-md"
				onInteractOutside={(event) => {
					if (temporaryPassword) event.preventDefault();
				}}
			>
				<DialogHeader className="items-center text-center">
					<span className="bg-muted mb-2 flex size-12 items-center justify-center rounded-full">
						<KeyRound className="size-6" />
					</span>
					<DialogTitle>
						{temporaryPassword ? t('users.title.reset_password_done') : t('users.title.reset_password')}
					</DialogTitle>
					<DialogDescription className="text-center">
						{temporaryPassword
							? t('users.description.reset_password_done', { name: user.name })
							: t('users.description.reset_password', { name: user.name })}
					</DialogDescription>
				</DialogHeader>

				{temporaryPassword ? (
					<>
						<CopyableCode value={temporaryPassword} size="md" />
						<DialogFooter className="mt-4">
							<DialogClose asChild>
								<Button className="w-full">{t('button.close')}</Button>
							</DialogClose>
						</DialogFooter>
					</>
				) : (
					<DialogFooter className="mt-4 grid grid-cols-2 gap-2">
						<DialogClose asChild>
							<Button variant="outline" className="w-full" disabled={isLoading}>
								{t('button.cancel')}
							</Button>
						</DialogClose>
						<Button className="w-full" disabled={isLoading} onClick={() => reset(user.id)}>
							{isLoading ? t('users.actions.resetting') : t('users.actions.reset_password')}
						</Button>
					</DialogFooter>
				)}
			</DialogContent>
		</Dialog>
	);
}
