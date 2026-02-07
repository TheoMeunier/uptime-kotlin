import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import setupService from '@/features/setup/services/setupService.ts';
import { toast } from 'sonner';
import { useNavigate } from 'react-router';
import useSetup from '@/features/setup/hooks/useSetupApp.ts';

const storeCreateFirstUserSchema = z
	.object({
		name: z.string().min(3),
		email: z.email(),
		password: z.string().min(8),
		password_confirmation: z.string().min(8),
	})
	.refine((data) => data.password === data.password_confirmation, {
		message: 'Passwords do not match',
		path: ['password_confirmation'],
	});

export type StoreCreateFirstUserSchemaType = z.infer<typeof storeCreateFirstUserSchema>;

export default function useStoreFirstUserApplication() {
	const navigate = useNavigate();
	const { updateSetupStatus } = useSetup();

	const form = useForm<StoreCreateFirstUserSchemaType>({
		resolver: zodResolver(storeCreateFirstUserSchema),
	});

	const mutation = useMutation({
		mutationFn: async (data: StoreCreateFirstUserSchemaType) => {
			return setupService.createFirstUserApplication(data);
		},
		onSuccess: () => {
			toast.success('First user created');
			updateSetupStatus(true);
			navigate('/login', { replace: true });
		},
	});

	const onSubmit = async (data: StoreCreateFirstUserSchemaType) => {
		mutation.mutate(data);
	};

	return {
		form,
		onSubmit,
		isLoading: mutation.isPending,
		errors: form.formState.errors,
	};
}
