import { useQuery } from '@tanstack/react-query';
import { z } from 'zod';

const DemoCredentialsSchema = z.object({ email: z.string(), password: z.string() });
export type DemoCredentials = z.infer<typeof DemoCredentialsSchema>;
const AppStatusSchema = z.object({ demo: DemoCredentialsSchema.nullish() });

export default function useDemoCredentials() {
	return useQuery({
		queryKey: ['demo-credentials'],
		queryFn: async (): Promise<DemoCredentials | null> => {
			const response = await fetch('/api/app/status');
			if (!response.ok) return null;

			return AppStatusSchema.parse(await response.json()).demo ?? null;
		},
		staleTime: Infinity,
		retry: false,
		meta: { silentError: true },
	});
}
