import { useTranslation } from 'react-i18next';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import userService from '@/features/users/services/user-service.ts';
import { invalidateUsers } from '@/features/users/hooks/useUsers.ts';

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
