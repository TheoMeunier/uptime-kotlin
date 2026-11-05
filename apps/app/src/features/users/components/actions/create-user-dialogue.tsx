import { useTranslation } from 'react-i18next';
import { UserPlus } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Dialog, DialogContent, DialogTrigger } from '@/components/atoms/dialog.tsx';
import UserForm from '@/features/users/components/forms/user-form.tsx';
import useStoreUser from '@/features/users/hooks/useStoreUser.ts';

export default function CreateUserDialogue() {
	const { t } = useTranslation();
	const { open, setOpen, onSubmit, isLoading } = useStoreUser();

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger asChild>
				<Button>
					<UserPlus className="size-4" /> {t('users.actions.create')}
				</Button>
			</DialogTrigger>

			<DialogContent className="sm:max-w-md" onInteractOutside={(event) => event.preventDefault()}>
				<UserForm onSubmit={onSubmit} isLoading={isLoading} />
			</DialogContent>
		</Dialog>
	);
}
