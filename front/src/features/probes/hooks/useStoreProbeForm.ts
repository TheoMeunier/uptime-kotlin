import {z} from "zod";
import {useNavigate} from "react-router";
import {type SubmitHandler, useForm} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import {useMutation} from "@tanstack/react-query";
import probeService from "@/features/probes/services/probeService.ts";
import ProbeProtocol from "@/features/probes/enums/probe-enum.ts";
import HttpStatusCode from "@/features/probes/enums/http-status-code.ts";

const baseStoreProbeSchema = z.object({
    name: z.string().min(3).max(255),
    interval: z.number().min(10).max(3600),
    retry: z.number().min(1).max(10).optional(),
    interval_retry: z.number().min(0).max(3600).optional(),
    timeout: z.number().min(10).max(3600).optional(),
    enabled: z.boolean().optional(),
    description: z.string().transform(val => val === "" ? null : val).optional(),
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

export function useStoreProbeForm() {
    const navigate = useNavigate();

    const form = useForm<StoreProbeSchema>({
        resolver: zodResolver(storeProbeSchema),
        defaultValues: {
            protocol: ProbeProtocol.HTTP,
            interval: 60,
            interval_retry: 60,
            retry: 3,
            timeout: 30,
            enabled: true,
            dns_server: '1.1.1.1',
            dns_port: 53,
            ping_delay: 2,
            ping_size: 56,
            ping_max_packet: 3,
            ignore_certificate_errors: false,
            notification_certificate: false,
            http_code_allowed: [],
        }
    })

    const mutation = useMutation({
        mutationFn: async (data: StoreProbeSchema) => {
            return probeService.storeProbe(data)
        },
        onSuccess: () => navigate('/dashboard'),
    })

    const onsubmit: SubmitHandler<StoreProbeSchema> = async (data: StoreProbeSchema) => {
        console.log(data)
        mutation.mutate(data);
    }

    return {
        form, onsubmit, isLoading: mutation.isPending, error: mutation.error
    }
}