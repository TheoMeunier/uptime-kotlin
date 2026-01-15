import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import HttpStatusCode from '@/features/probes/enums/http-status-code.ts';

const baseStoreProbeSchema = z.object({
	name: z.string().min(3).max(255),
	interval: z.number().min(10).max(3600),
	retry: z.number().min(1).max(10).optional(),
	interval_retry: z.number().min(0).max(3600).optional(),
	//timeout: z.number().min(10).max(3600).optional(),
	enabled: z.boolean().optional(),
	description: z
		.string()
		.transform((val) => (val === '' ? null : val))
		.nullish(),
	notifications: z.array(z.uuid()).optional(),
});

const httpProbeSchema = baseStoreProbeSchema.extend({
	protocol: z.literal('HTTP'),
	notification_certificate: z.boolean(),
	ignore_certificate_errors: z.boolean(),
	http_code_allowed: z.enum(HttpStatusCode).array(),
	url: z.url(),
});

const tcpProbeSchema = baseStoreProbeSchema.extend({
	protocol: z.literal('TCP'),
	url: z.ipv4(),
	tcp_port: z.number().min(1).max(65535),
});

const pingProbeSchema = baseStoreProbeSchema.extend({
	protocol: z.literal('PING'),
	url: z.url(),
	ping_heartbeat_interval: z.number().min(1).max(60),
	ping_max_packet: z.number().min(1).max(10),
	ping_size: z.number().min(32).max(65500),
	ping_delay: z.number().min(1).max(60),
	ping_numeric_output: z.boolean().optional(),
});

const dnsProbeSchema = baseStoreProbeSchema.extend({
	protocol: z.literal('DNS'),
	url: z.url(),
	dns_server: z.ipv4(),
	dns_port: z.number().min(1).max(65535).optional(),
});

export const storeProbeSchema = z.discriminatedUnion('protocol', [
	httpProbeSchema,
	tcpProbeSchema,
	pingProbeSchema,
	dnsProbeSchema,
]);

export type StoreProbeSchema = z.infer<typeof storeProbeSchema>;

export function useProbeForm({ defaultValues }: { defaultValues: Partial<StoreProbeSchema> } = { defaultValues: {} }) {
	const form = useForm<StoreProbeSchema>({
		resolver: zodResolver(storeProbeSchema),
		defaultValues: {
			...defaultValues,
		},
	});

	return {
		form,
		errors: form.formState.errors,
	};
}
