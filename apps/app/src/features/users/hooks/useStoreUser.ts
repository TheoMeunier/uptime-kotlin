import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import userService from '@/features/users/services/user-service.ts';
import { invalidateUsers } from '@/features/users/hooks/useUsers.ts';
import type { UserFormValues } from '@/features/users/hooks/useUserForm.ts';

export default function useStoreUser() {
	const { t } = useTranslation();
	const client = useQueryClient();
	const [open, setOpen] = useState(false);

	const mutation = useMutation({
		mutationFn: (values: UserFormValues) => userService.storeUser(values),
		onSuccess: async (_, values) => {
			await invalidateUsers(client);
			setOpen(false);
			toast.success(t('users.alerts.created', { name: values.name }));
		},
	});

	return {
		open,
		setOpen,
		onSubmit: (values: UserFormValues) => mutation.mutate(values),
		isLoading: mutation.isPending,
	};
}
