import { useTranslation } from 'react-i18next';
import { UserMinus } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import ConfirmDialogue from '@/components/molecules/confirm-dialogue.tsx';
import { useRemoveUser } from '@/features/users/hooks/useUserActions.ts';
import type { User } from '@/features/users/schemas/user.schema.ts';

export default function RemoveUserDialogue({ user, disabled }: { user: User; disabled: boolean }) {
	const { t } = useTranslation();
	const { remove, isLoading } = useRemoveUser();

	return (
		<ConfirmDialogue
			trigger={
				<Button
					variant="outline"
					size="sm"
					disabled={disabled}
					aria-label={t('button.actions.remove')}
					className="text-status-down-fg hover:bg-status-down-bg hover:text-status-down-fg"
				>
					<UserMinus className="size-4" />
				</Button>
			}
			icon={UserMinus}
			title={t('users.title.remove')}
			description={t('users.description.remove', { name: user.name })}
			confirmLabel={t('button.actions.remove')}
			onConfirm={() => remove(user.id)}
			isPending={isLoading}
		/>
	);
}
