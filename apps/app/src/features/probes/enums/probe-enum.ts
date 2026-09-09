enum ProbeProtocol {
	HTTP = 'HTTP',
	TCP = 'TCP',
	PING = 'PING',
	DNS = 'DNS',
	POSTGRESQL = 'POSTGRESQL',
	SQLSERVER = 'MICROSOFT SQL SERVER',
	MYSQL = 'MYSQL / MARIADB',
	REDIS = 'REDIS',
	SMTP = 'SMTP',
	KAFKA = 'KAFKA PRODUCER',
	RABBITMQ = 'RABBITMQ',
}

export const PROTOCOL_LABELS: Record<ProbeProtocol, string> = {
	[ProbeProtocol.HTTP]: 'HTTP',
	[ProbeProtocol.TCP]: 'TCP',
	[ProbeProtocol.PING]: 'Ping',
	[ProbeProtocol.DNS]: 'DNS',
	[ProbeProtocol.POSTGRESQL]: 'PostgreSQL',
	[ProbeProtocol.SQLSERVER]: 'Microsoft SQL Server',
	[ProbeProtocol.MYSQL]: 'MySQL / MariaDB',
	[ProbeProtocol.REDIS]: 'Redis',
	[ProbeProtocol.SMTP]: 'SMTP',
	[ProbeProtocol.KAFKA]: 'Kafka producer',
	[ProbeProtocol.RABBITMQ]: 'RabbitMQ',
};

export const PROTOCOL_GROUPS: { labelKey: string; protocols: ProbeProtocol[] }[] = [
	{
		labelKey: 'monitors.protocol_group.network',
		protocols: [ProbeProtocol.HTTP, ProbeProtocol.TCP, ProbeProtocol.PING, ProbeProtocol.DNS],
	},
	{
		labelKey: 'monitors.protocol_group.database',
		protocols: [ProbeProtocol.POSTGRESQL, ProbeProtocol.MYSQL, ProbeProtocol.SQLSERVER, ProbeProtocol.REDIS],
	},
	{
		labelKey: 'monitors.protocol_group.messaging',
		protocols: [ProbeProtocol.SMTP, ProbeProtocol.KAFKA, ProbeProtocol.RABBITMQ],
	},
];

export default ProbeProtocol;
