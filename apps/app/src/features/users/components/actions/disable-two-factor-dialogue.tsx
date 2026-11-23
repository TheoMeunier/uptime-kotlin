import { useTranslation } from 'react-i18next';
import { ShieldOff } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import ConfirmDialogue from '@/components/molecules/confirm-dialogue.tsx';
import { useDisableUserTwoFactor } from '@/features/users/hooks/useUserActions.ts';
import type { User } from '@/features/users/schemas/user.schema.ts';

export default function DisableTwoFactorUserDialogue({ user }: { user: User }) {
	const { t } = useTranslation();
	const { disable, isLoading } = useDisableUserTwoFactor();

	return (
		<ConfirmDialogue
			trigger={
				<Button
					variant="outline"
					size="sm"
					aria-label={t('two_factor.admin.disable')}
					title={t('two_factor.admin.disable')}
				>
					<ShieldOff className="size-4" />
				</Button>
			}
			icon={ShieldOff}
			title={t('two_factor.admin.disable_title')}
			description={t('two_factor.admin.disable_description', { name: user.name })}
			confirmLabel={t('two_factor.admin.disable')}
			onConfirm={() => disable(user.id)}
			isPending={isLoading}
		/>
	);
}
