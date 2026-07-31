import ProbeProtocol from '@/features/probes/enums/probe-enum.ts';
import HttpStatusCode from '@/features/probes/enums/http-status-code.ts';
import i18n from '@/lang/i18n.ts';
import DNSRecord from '@/features/probes/enums/dns-record.ts';

const PROBE_FIELDS_CONFIG = {
	[ProbeProtocol.HTTP]: {
		fields: [
			{
				name: 'url',
				label: i18n.t('form.label.url'),
				input_type: 'text',
				placeholder: 'https://',
			},
			{
				name: 'method',
				label: i18n.t('monitors.label.http_method'),
				input_type: 'select',
				options: ['GET', 'HEAD', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
				default_value: 'GET',
			},
		],
		advanced_fields: [
			{
				name: 'follow_redirects',
				label: i18n.t('monitors.label.follow_redirects'),
				input_type: 'switch',
				default_value: true,
			},
			{
				name: 'max_latency_ms',
				label: i18n.t('monitors.label.max_latency_ms'),
				input_type: 'number',
			},
			{
				name: 'tls_expiry_warning_days',
				label: i18n.t('monitors.label.tls_expiry_warning_days'),
				input_type: 'number',
				default_value: 30,
			},
			{
				name: 'notification_certificate',
				label: i18n.t('monitors.label.notification_certificate'),
				input_type: 'switch',
				default_value: false,
			},
			{
				name: 'ignore_certificate_errors',
				label: i18n.t('monitors.label.ignore_certificate_errors'),
				input_type: 'switch',
				default_value: false,
			},
			{
				name: 'http_code_allowed',
				label: i18n.t('monitors.label.http_code_allowed'),
				input_type: 'switch_multiple',
				options: Object.values(HttpStatusCode),
				searchable: false,
				closeOnSelect: false,
			},
		],
	},
	[ProbeProtocol.TCP]: {
		fields: [
			{
				name: 'url',
				label: i18n.t('monitors.label.tcp_host'),
				input_type: 'text',
			},
			{
				name: 'tcp_port',
				label: i18n.t('monitors.label.tcp_port'),
				input_type: 'text',
			},
		],
		advanced_fields: [],
	},
	[ProbeProtocol.DNS]: {
		fields: [
			{
				name: 'hostname',
				label: i18n.t('form.label.url'),
				input_type: 'text',
			},
			{
				name: 'dns_server',
				label: i18n.t('monitors.label.dns_server'),
				input_type: 'text',
				default_value: '1.1.1.1',
				description: i18n.t('monitors.description.dns_server'),
			},
			{
				name: 'dns_port',
				label: i18n.t('monitors.label.dns_port'),
				input_type: 'number',
				default_value: 53,
				description: i18n.t('monitors.description.dns_port'),
			},
			{
				name: 'record_type',
				label: i18n.t('monitors.label.dns_record'),
				input_type: 'select',
				options: Object.values(DNSRecord),
				default_value: false,
			},
		],
		advanced_fields: [],
	},
	[ProbeProtocol.PING]: {
		fields: [
			{
				name: 'ip',
				label: i18n.t('form.label.url'),
				input_type: 'text',
			},
			{
				name: 'ping_heartbeat_interval',
				label: i18n.t('monitors.label.ping_heartbeat_interval'),
				input_type: 'number',
			},
		],
		advanced_fields: [
			{
				name: 'ping_max_packet',
				label: i18n.t('monitors.label.ping_max_packet'),
				input_type: 'number',
				default_value: 3,
			},
			{
				name: 'ping_size',
				label: i18n.t('monitors.label.ping_size'),
				input_type: 'number',
				default_value: 56,
			},
			{
				name: 'ping_delay',
				label: i18n.t('monitors.label.ping_size'),
				input_type: 'number',
				default_value: 2,
			},
		],
	},
	[ProbeProtocol.POSTGRESQL]: {
		fields: [
			{
				name: 'connection_string',
				label: i18n.t('monitors.label.postgresql_connection_string'),
				input_type: 'text',
				placeholder: 'postgres://username:password@host:5432/database',
			},
			{
				name: 'query',
				label: i18n.t('monitors.label.postgresql_query'),
				input_type: 'textarea',
				placeholder: 'SELECT 1',
				default_value: 'SELECT 1',
			},
		],
		advanced_fields: [],
	},
	[ProbeProtocol.SQLSERVER]: {
		fields: [
			{
				name: 'connection_string',
				label: i18n.t('monitors.label.sqlserver_connection_string'),
				input_type: 'text',
				placeholder: 'sqlserver://username:password@host:1433/database',
			},
			{
				name: 'query',
				label: i18n.t('monitors.label.sqlserver_query'),
				input_type: 'textarea',
				placeholder: 'SELECT 1',
				default_value: 'SELECT 1',
			},
		],
		advanced_fields: [],
	},
	[ProbeProtocol.MYSQL]: {
		fields: [
			{
				name: 'connection_string',
				label: i18n.t('monitors.label.mysql_connection_string'),
				input_type: 'text',
				placeholder: 'mysql://username:password@host:3306/database',
			},
			{
				name: 'query',
				label: i18n.t('monitors.label.mysql_query'),
				input_type: 'textarea',
				placeholder: 'SELECT 1',
				default_value: 'SELECT 1',
			},
		],
		advanced_fields: [],
	},
	[ProbeProtocol.REDIS]: {
		fields: [
			{
				name: 'connection_string',
				label: i18n.t('monitors.label.redis_connection_string'),
				input_type: 'text',
				placeholder: 'redis://username:password@host:6379/0',
			},
			{
				name: 'command',
				label: i18n.t('monitors.label.redis_command'),
				input_type: 'textarea',
				placeholder: 'PING',
				default_value: 'PING',
			},
		],
		advanced_fields: [],
	},
	[ProbeProtocol.SMTP]: {
		fields: [
			{
				name: 'hostname',
				label: i18n.t('monitors.label.smtp_hostname'),
				input_type: 'text',
				placeholder: 'smtp.example.com',
			},
			{
				name: 'port',
				label: i18n.t('monitors.label.smtp_port'),
				input_type: 'number',
				placeholder: '587',
			},
			{
				name: 'security',
				label: i18n.t('monitors.label.smtp_security'),
				input_type: 'select',
				options: [
					{ value: 'SMTPS', label: 'SMTPS' },
					{ value: 'IGNORE_TLS', label: i18n.t('monitors.option.smtp_ignore_tls') },
					{ value: 'STARTTLS', label: 'STARTTLS' },
				],
				description: i18n.t('monitors.description.smtp_security'),
			},
		],
		advanced_fields: [],
	},
};

export default PROBE_FIELDS_CONFIG;
