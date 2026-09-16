import ProbeProtocol from '@/features/probes/enums/probe-enum.ts';
import HttpStatusCode from '@/features/probes/enums/http-status-code.ts';
import type { TFunction } from 'i18next';
import DNSRecord from '@/features/probes/enums/dns-record.ts';

// Fabrique, et non constante : évaluée au chargement du module, `t()` figerait tous les
// libellés dans la langue détectée au démarrage et le sélecteur de langue n'aurait aucun
// effet sur ce formulaire.
export function buildProbeFieldsConfig(t: TFunction) {
	return {
		[ProbeProtocol.HTTP]: {
			fields: [
				{
					name: 'url',
					label: t('form.label.url'),
					input_type: 'text',
					placeholder: 'https://',
				},
				{
					name: 'method',
					label: t('monitors.label.http_method'),
					input_type: 'select',
					options: ['GET', 'HEAD', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
					default_value: 'GET',
				},
			],
			advanced_fields: [
				{
					name: 'follow_redirects',
					label: t('monitors.label.follow_redirects'),
					input_type: 'switch',
					default_value: true,
				},
				{
					name: 'max_latency_ms',
					label: t('monitors.label.max_latency_ms'),
					input_type: 'number',
					default_value: 2000,
					min: 1,
					max: 5000,
					description: t('monitors.description.max_latency_ms'),
				},
				{
					name: 'tls_expiry_warning_days',
					label: t('monitors.label.tls_expiry_warning_days'),
					input_type: 'number',
					default_value: 30,
				},
				{
					name: 'notification_certificate',
					label: t('monitors.label.notification_certificate'),
					input_type: 'switch',
					default_value: false,
				},
				{
					name: 'ignore_certificate_errors',
					label: t('monitors.label.ignore_certificate_errors'),
					input_type: 'switch',
					default_value: false,
				},
				{
					name: 'http_code_allowed',
					label: t('monitors.label.http_code_allowed'),
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
					label: t('monitors.label.tcp_host'),
					input_type: 'text',
				},
				{
					name: 'tcp_port',
					label: t('monitors.label.tcp_port'),
					input_type: 'text',
				},
			],
			advanced_fields: [],
		},
		[ProbeProtocol.DNS]: {
			fields: [
				{
					name: 'hostname',
					label: t('form.label.url'),
					input_type: 'text',
				},
				{
					name: 'dns_server',
					label: t('monitors.label.dns_server'),
					input_type: 'text',
					default_value: '1.1.1.1',
					description: t('monitors.description.dns_server'),
				},
				{
					name: 'dns_port',
					label: t('monitors.label.dns_port'),
					input_type: 'number',
					default_value: 53,
					description: t('monitors.description.dns_port'),
				},
				{
					name: 'record_type',
					label: t('monitors.label.dns_record'),
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
					label: t('form.label.url'),
					input_type: 'text',
				},
				{
					name: 'ping_heartbeat_interval',
					label: t('monitors.label.ping_heartbeat_interval'),
					input_type: 'number',
				},
			],
			advanced_fields: [
				{
					name: 'ping_max_packet',
					label: t('monitors.label.ping_max_packet'),
					input_type: 'number',
					default_value: 3,
				},
				{
					name: 'ping_size',
					label: t('monitors.label.ping_size'),
					input_type: 'number',
					default_value: 56,
				},
				{
					name: 'ping_delay',
					label: t('monitors.label.ping_size'),
					input_type: 'number',
					default_value: 2,
				},
			],
		},
		[ProbeProtocol.POSTGRESQL]: {
			fields: [
				{
					name: 'connection_string',
					label: t('monitors.label.postgresql_connection_string'),
					input_type: 'text',
					default_value: 'postgres://username:password@host:5432/database',
				},
				{
					name: 'query',
					label: t('monitors.label.postgresql_query'),
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
					label: t('monitors.label.sqlserver_connection_string'),
					input_type: 'text',
					default_value: 'sqlserver://username:password@host:1433/database',
				},
				{
					name: 'query',
					label: t('monitors.label.sqlserver_query'),
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
					label: t('monitors.label.mysql_connection_string'),
					input_type: 'text',
					default_value: 'mysql://username:password@host:3306/database',
				},
				{
					name: 'query',
					label: t('monitors.label.mysql_query'),
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
					label: t('monitors.label.redis_connection_string'),
					input_type: 'text',
					default_value: 'redis://username:password@host:6379/0',
				},
				{
					name: 'command',
					label: t('monitors.label.redis_command'),
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
					label: t('monitors.label.smtp_hostname'),
					input_type: 'text',
					default_value: 'smtp.example.com',
				},
				{
					name: 'port',
					label: t('monitors.label.smtp_port'),
					input_type: 'number',
					default_value: 587,
				},
				{
					name: 'security',
					label: t('monitors.label.smtp_security'),
					input_type: 'select',
					options: [
						{ value: 'SMTPS', label: 'SMTPS' },
						{ value: 'IGNORE_TLS', label: t('monitors.option.smtp_ignore_tls') },
						{ value: 'STARTTLS', label: 'STARTTLS' },
					],
					description: t('monitors.description.smtp_security'),
				},
			],
			advanced_fields: [],
		},
		[ProbeProtocol.KAFKA]: {
			fields: [
				{
					name: 'brokers',
					label: t('monitors.label.kafka_brokers'),
					input_type: 'text',
					placeholder: 'kafka-1:9092,kafka-2:9092',
				},
				{
					name: 'topic',
					label: t('monitors.label.kafka_topic'),
					input_type: 'text',
				},
				{
					name: 'message',
					label: t('monitors.label.kafka_message'),
					input_type: 'text',
				},
				{
					name: 'ssl',
					label: t('monitors.label.kafka_ssl'),
					input_type: 'switch',
					default_value: false,
				},
				{
					name: 'allow_auto_topic_creation',
					label: t('monitors.label.kafka_auto_topic_creation'),
					input_type: 'switch',
					default_value: false,
				},
			],
			advanced_fields: [],
		},
		[ProbeProtocol.RABBITMQ]: {
			fields: [
				{
					name: 'management_nodes',
					label: t('monitors.label.rabbitmq_management_nodes'),
					input_type: 'text',
					placeholder: 'https://node1.rabbitmq.com:15672,https://node2.rabbitmq.com:15672',
					default_value: 'https://localhost:15672',
					description: t('monitors.description.rabbitmq_management_nodes'),
				},
				{
					name: 'username',
					label: t('monitors.label.rabbitmq_username'),
					input_type: 'text',
				},
				{
					name: 'password',
					label: t('monitors.label.rabbitmq_password'),
					input_type: 'password',
				},
			],
			advanced_fields: [],
		},
	};
}

export type ProbeFieldsConfig = ReturnType<typeof buildProbeFieldsConfig>;
