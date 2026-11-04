enum ProbeProtocol {
	HTTP = 'HTTP',
	TCP = 'TCP',
	WEBSOCKET = 'WEBSOCKET UPGRADE',
	PING = 'PING',
	DNS = 'DNS',
	POSTGRESQL = 'POSTGRESQL',
	SQLSERVER = 'MICROSOFT SQL SERVER',
	MYSQL = 'MYSQL / MARIADB',
	REDIS = 'REDIS',
	MONGODB = 'MONGODB',
	ORACLE = 'ORACLE',
	SMTP = 'SMTP',
	KAFKA = 'KAFKA PRODUCER',
	RABBITMQ = 'RABBITMQ',
	DOCKER = 'DOCKER CONTAINER',
}

export const PROTOCOL_LABELS: Record<ProbeProtocol, string> = {
	[ProbeProtocol.HTTP]: 'HTTP',
	[ProbeProtocol.TCP]: 'TCP',
	[ProbeProtocol.WEBSOCKET]: 'WebSocket Upgrade',
	[ProbeProtocol.PING]: 'Ping',
	[ProbeProtocol.DNS]: 'DNS',
	[ProbeProtocol.POSTGRESQL]: 'PostgreSQL',
	[ProbeProtocol.SQLSERVER]: 'Microsoft SQL Server',
	[ProbeProtocol.MYSQL]: 'MySQL / MariaDB',
	[ProbeProtocol.REDIS]: 'Redis',
	[ProbeProtocol.MONGODB]: 'MongoDB',
	[ProbeProtocol.ORACLE]: 'Oracle Database',
	[ProbeProtocol.SMTP]: 'SMTP',
	[ProbeProtocol.KAFKA]: 'Kafka producer',
	[ProbeProtocol.RABBITMQ]: 'RabbitMQ',
	[ProbeProtocol.DOCKER]: 'Docker container',
};

export const PROTOCOL_GROUPS: { labelKey: string; protocols: ProbeProtocol[] }[] = [
	{
		labelKey: 'monitors.protocol_group.network',
		protocols: [ProbeProtocol.HTTP, ProbeProtocol.WEBSOCKET, ProbeProtocol.TCP, ProbeProtocol.PING, ProbeProtocol.DNS],
	},
	{
		labelKey: 'monitors.protocol_group.database',
		protocols: [
			ProbeProtocol.POSTGRESQL,
			ProbeProtocol.MYSQL,
			ProbeProtocol.SQLSERVER,
			ProbeProtocol.ORACLE,
			ProbeProtocol.REDIS,
			ProbeProtocol.MONGODB,
		],
	},
	{
		labelKey: 'monitors.protocol_group.messaging',
		protocols: [ProbeProtocol.SMTP, ProbeProtocol.KAFKA, ProbeProtocol.RABBITMQ],
	},
	{
		labelKey: 'monitors.protocol_group.containers',
		protocols: [ProbeProtocol.DOCKER],
	},
];

export default ProbeProtocol;
