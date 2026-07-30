import { z } from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import HttpStatusCode from '@/features/probes/enums/http-status-code.ts';

const baseStoreProbeSchema = z.object({
	name: z.string().min(3).max(255),
	interval: z.number().min(10).max(3600),
	retry: z.number().min(1).max(10).optional(),
	interval_retry: z.number().min(0).max(3600).optional(),
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
	method: z.enum(['GET', 'HEAD', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS']).optional(),
	headers: z.record(z.string(), z.string()).optional(),
	body: z.string().nullable().optional(),
	authentication: z
		.object({
			type: z.enum(['BASIC', 'BEARER']),
			username: z.string().optional(),
			password: z.string().optional(),
			token: z.string().optional(),
		})
		.nullable()
		.optional(),
	assertions: z
		.array(
			z.object({
				type: z.enum(['TEXT_CONTAINS', 'JSON_EQUALS', 'RESPONSE_HEADER_EQUALS']),
				expected: z.string(),
				path: z.string().optional(),
				header: z.string().optional(),
			})
		)
		.optional(),
	follow_redirects: z.boolean().optional(),
	max_latency_ms: z.number().positive().optional(),
	tls_expiry_warning_days: z.union([z.literal(7), z.literal(15), z.literal(30)]).optional(),
	steps: z.array(z.record(z.string(), z.unknown())).optional(),
});

const tcpProbeSchema = baseStoreProbeSchema.extend({
	protocol: z.literal('TCP'),
	url: z.ipv4(),
	tcp_port: z.number().min(1).max(65535),
});

const pingProbeSchema = baseStoreProbeSchema.extend({
	protocol: z.literal('PING'),
	ip: z.url(),
	ping_heartbeat_interval: z.number().min(1).max(60),
	ping_max_packet: z.number().min(1).max(10),
	ping_size: z.number().min(32).max(65500),
	ping_delay: z.number().min(1).max(60),
	ping_numeric_output: z.boolean().optional(),
});

const dnsProbeSchema = baseStoreProbeSchema.extend({
	protocol: z.literal('DNS'),
	hostname: z.url(),
	dns_server: z.ipv4(),
	dns_port: z.number().min(1).max(65535).optional(),
	record_type: z.enum(['A', 'AAAA', 'CNAME', 'MX', 'TXT']).optional(),
});

const postgreSqlProbeSchema = baseStoreProbeSchema.extend({
	protocol: z.literal('POSTGRESQL'),
	connection_string: z.string().regex(/^postgres(?:ql)?:\/\/\S+$/),
	query: z.string().min(1),
});

const mySqlProbeSchema = baseStoreProbeSchema.extend({
	protocol: z.literal('MYSQL / MARIADB'),
	connection_string: z.string().regex(/^(?:mysql|mariadb):\/\/\S+$/),
	query: z.string().min(1),
});

export const storeProbeSchema = z.discriminatedUnion('protocol', [
	httpProbeSchema,
	tcpProbeSchema,
	pingProbeSchema,
	dnsProbeSchema,
	postgreSqlProbeSchema,
	mySqlProbeSchema,
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
