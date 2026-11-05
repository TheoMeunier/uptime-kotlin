import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { UserRoleEnum } from '@/features/users/schemas/user.schema.ts';

export const userFormSchema = z.object({
	name: z.string().trim().min(1).max(255),
	email: z.email(),
	password: z.string().min(8),
	role: z.enum(UserRoleEnum),
});

export type UserFormValues = z.infer<typeof userFormSchema>;

export default function useUserForm() {
	const form = useForm<UserFormValues>({
		resolver: zodResolver(userFormSchema),
		defaultValues: {
			name: '',
			email: '',
			password: '',
			role: 'VIEWER',
		},
	});

	return { form, errors: form.formState.errors };
}
