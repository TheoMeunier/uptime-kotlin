import { useTranslation } from 'react-i18next';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import userService from '@/features/users/services/user-service.ts';
import { invalidateUsers } from '@/features/users/hooks/useUsers.ts';
import type { UserRole } from '@/features/users/schemas/user.schema.ts';

export function useUpdateUserRole() {
	const { t } = useTranslation();
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: ({ userId, role }: { userId: string; role: UserRole }) => userService.updateRole(userId, role),
		onSuccess: () => toast.success(t('users.alerts.role_updated')),
		onSettled: () => invalidateUsers(client),
	});

	return { update: mutation.mutate, isLoading: mutation.isPending };
}

export function useRemoveUser() {
	const { t } = useTranslation();
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: (userId: string) => userService.removeUser(userId),
		onSuccess: () => toast.success(t('users.alerts.removed')),
		onSettled: () => invalidateUsers(client),
	});

	return { remove: mutation.mutate, isLoading: mutation.isPending };
}
