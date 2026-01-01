import ProbeProtocol from "@/features/probes/enums/probe-enum.ts";
import HttpStatusCode from "@/features/probes/enums/http-status-code.ts";

const PROBE_FIELDS_CONFIG = {
  [ProbeProtocol.HTTP]: {
    fields: [
      {
        name: "url",
        label: "URL",
        input_type: "text",
        placeholder: "https://",
      },
    ],
    advanced_fields: [
      {
        name: "notification_certificate",
        label: "Certificate Expiry Notification",
        input_type: "switch",
        default_value: false,
      },
      {
        name: "ignore_certificate_errors",
        label: "Ignore TLS / SSL errors for HTTS websites",
        input_type: "switch",
        default_value: false,
      },
      {
        name: "http_code_allowed",
        label: "Accepted Status Codes",
        input_type: "switch_multiple",
        options: Object.values(HttpStatusCode),
        default_value: [],
        searchable: false,
        closeOnSelect: false,
      },
    ],
  },
  [ProbeProtocol.TCP]: {
    fields: [
      {
        name: "url",
        label: "TCP host",
        input_type: "text",
      },
      {
        name: "tcp_port",
        label: "TCP port",
        input_type: "text",
      },
    ],
    advanced_fields: [],
  },
  [ProbeProtocol.DNS]: {
    fields: [
      {
        name: "url",
        label: "Host",
        input_type: "text",
      },
      {
        name: "dns_server",
        label: "DNS server",
        input_type: "text",
        default_value: "1.1.1.1",
        description:
          "Cloudflare is the default server. You can change the resolver server anytime.",
      },
      {
        name: "dns_port",
        label: "DNS port",
        input_type: "number",
        default_value: 53,
        description:
          "DNS server port. Defaults to 53. You can change the port at any time.",
      },
    ],
    advanced_fields: [],
  },
  [ProbeProtocol.PING]: {
    fields: [
      {
        name: "url",
        label: "Host",
        input_type: "text",
      },
      {
        name: "ping_heartbeat_interval",
        label: "Heartbeat Interval",
        input_type: "number",
      },
    ],
    advanced_fields: [
      {
        name: "ping_max_packet",
        label: "Number of ping packets",
        input_type: "number",
        default_value: 3,
      },
      {
        name: "ping_size",
        label: "Numeric Output",
        input_type: "number",
        default_value: 56,
      },
      {
        name: "ping_delay",
        label: "Ping delay (in seconds)",
        input_type: "number",
        default_value: 2,
      },
    ],
  },
};

export default PROBE_FIELDS_CONFIG;
