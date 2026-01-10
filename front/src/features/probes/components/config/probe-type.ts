import ProbeProtocol from "@/features/probes/enums/probe-enum.ts";
import HttpStatusCode from "@/features/probes/enums/http-status-code.ts";
import i18n from "@/lang/i18n.ts";

const PROBE_FIELDS_CONFIG = {
  [ProbeProtocol.HTTP]: {
    fields: [
      {
        name: "url",
        label: i18n.t("form.label.url"),
        input_type: "text",
        placeholder: "https://",
      },
    ],
    advanced_fields: [
      {
        name: "notification_certificate",
        label: i18n.t("monitors.label.notification_certificate"),
        input_type: "switch",
        default_value: false,
      },
      {
        name: "ignore_certificate_errors",
        label: i18n.t("monitors.label.ignore_certificate_errors"),
        input_type: "switch",
        default_value: false,
      },
      {
        name: "http_code_allowed",
        label: i18n.t("monitors.label.http_code_allowed"),
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
        label: i18n.t("monitors.label.tcp_host"),
        input_type: "text",
      },
      {
        name: "tcp_port",
        label: i18n.t("monitors.label.tcp_port"),
        input_type: "text",
      },
    ],
    advanced_fields: [],
  },
  [ProbeProtocol.DNS]: {
    fields: [
      {
        name: "url",
        label: i18n.t("form.label.url"),
        input_type: "text",
      },
      {
        name: "dns_server",
        label: i18n.t("monitors.label.dns_server"),
        input_type: "text",
        default_value: "1.1.1.1",
        description: i18n.t("monitors.description.dns_server"),
      },
      {
        name: "dns_port",
        label: i18n.t("monitors.label.dns_port"),
        input_type: "number",
        default_value: 53,
        description: i18n.t("monitors.description.dns_port"),
      },
    ],
    advanced_fields: [],
  },
  [ProbeProtocol.PING]: {
    fields: [
      {
        name: "url",
        label: i18n.t("form.label.url"),
        input_type: "text",
      },
      {
        name: "ping_heartbeat_interval",
        label: i18n.t("monitors.label.ping_heartbeat_interval"),
        input_type: "number",
      },
    ],
    advanced_fields: [
      {
        name: "ping_max_packet",
        label: i18n.t("monitors.label.ping_max_packet"),
        input_type: "number",
        default_value: 3,
      },
      {
        name: "ping_size",
        label: i18n.t("monitors.label.ping_size"),
        input_type: "number",
        default_value: 56,
      },
      {
        name: "ping_delay",
        label: i18n.t("monitors.label.ping_size"),
        input_type: "number",
        default_value: 2,
      },
    ],
  },
};

export default PROBE_FIELDS_CONFIG;
