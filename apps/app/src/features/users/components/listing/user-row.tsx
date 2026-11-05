import { useTranslation } from 'react-i18next';
import { Avatar, AvatarFallback } from '@/components/atoms/avatar.tsx';
import { Badge } from '@/components/atoms/badge.tsx';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/atoms/select.tsx';
import { getInitials } from '@/lib/utils.ts';
import RemoveUserDialogue from '@/features/users/components/actions/remove-user-dialogue.tsx';
import UserRoleBadge from '@/features/users/components/listing/user-role-badge.tsx';
import { useUpdateUserRole } from '@/features/users/hooks/useUserActions.ts';
import { type User, type UserRole, UserRoleEnum } from '@/features/users/schemas/user.schema.ts';

export default function UserRow({ user, isLastAdmin }: { user: User; isLastAdmin: boolean }) {
	const { t } = useTranslation();
	const { update, isLoading } = useUpdateUserRole();
	const locked = user.current || isLastAdmin;

	return (
		<li className="flex flex-col gap-3 py-3 sm:flex-row sm:items-center sm:justify-between">
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
					</p>
					<p className="text-muted-foreground truncate text-xs">{user.email}</p>
				</div>
			</div>

			<div className="flex shrink-0 items-center gap-2 pl-11 sm:pl-0">
				{locked ? (
					<span title={t(user.current ? 'users.hint.own_account' : 'users.hint.last_admin')}>
						<UserRoleBadge role={user.role} />
					</span>
				) : (
					<Select
						value={user.role}
						disabled={isLoading}
						onValueChange={(role) => update({ userId: user.id, role: role as UserRole })}
					>
						<SelectTrigger className="w-40" aria-label={t('users.form.role')}>
							<SelectValue />
						</SelectTrigger>
						<SelectContent>
							{UserRoleEnum.map((role) => (
								<SelectItem key={role} value={role}>
									{t(`users.role.${role}`)}
								</SelectItem>
							))}
						</SelectContent>
					</Select>
				)}

				<RemoveUserDialogue user={user} disabled={locked} />
			</div>
		</li>
	);
}
