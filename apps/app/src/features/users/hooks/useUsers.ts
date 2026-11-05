import { type QueryClient, useQuery } from '@tanstack/react-query';
import userService from '@/features/users/services/user-service.ts';

export async function invalidateUsers(client: QueryClient): Promise<void> {
	await client.invalidateQueries({ queryKey: ['users'] });
}

export default function useUsers() {
	return useQuery({
		queryKey: ['users'],
		queryFn: () => userService.getUsers(),
	});
}
