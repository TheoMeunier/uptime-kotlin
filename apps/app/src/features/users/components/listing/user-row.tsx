import { useTranslation } from 'react-i18next';
import { Avatar, AvatarFallback } from '@/components/atoms/avatar.tsx';
import { Badge } from '@/components/atoms/badge.tsx';
import { getInitials } from '@/lib/utils.ts';
import RemoveUserDialogue from '@/features/users/components/actions/remove-user-dialogue.tsx';
import ResetPasswordDialogue from '@/features/users/components/actions/reset-password-dialogue.tsx';
import UserRoleBadge from '@/features/users/components/listing/user-role-badge.tsx';
import type { User } from '@/features/users/schemas/user.schema.ts';

export default function UserRow({ user, isLastAdmin }: { user: User; isLastAdmin: boolean }) {
	const { t } = useTranslation();

	return (
		<li className="flex items-center justify-between gap-4 py-3">
			<div className="flex min-w-0 items-center gap-3">
				<Avatar className="size-8 rounded-lg">
					<AvatarFallback className="rounded-lg text-xs">{getInitials(user.name)}</AvatarFallback>
				</Avatar>
				<div className="min-w-0">
					<p className="flex items-center gap-2 text-sm font-medium">
						<span className="truncate">{user.name}</span>
						{user.current && (
							<Badge variant="outline" className="font-normal">
								{t('users.you')}
							</Badge>
						)}
						{user.must_change_password && (
							<Badge variant="outline" className="font-normal" title={t('users.reset_pending_hint')}>
								{t('users.reset_pending')}
							</Badge>
						)}
					</p>
					<p className="text-muted-foreground truncate text-xs">{user.email}</p>
				</div>
			</div>

			<div className="flex shrink-0 items-center gap-2">
				<UserRoleBadge role={user.role} />
				<ResetPasswordDialogue user={user} disabled={user.current} />
				<RemoveUserDialogue user={user} disabled={user.current || isLastAdmin} />
			</div>
		</li>
	);
}
