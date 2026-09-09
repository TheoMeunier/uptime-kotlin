const en = {
	errors: {
		load: {
			title: 'Could not load this data',
			description: 'The API did not answer. It may be restarting, or unreachable from your browser.',
		},
	},

	app: {
		loading: 'Loading Uptime Kotlin',
	},

	status: {
		healthy: 'Healthy',
		degraded: 'Degraded',
		unhealthy: 'Unhealthy',
		paused: 'Paused',
	},

	form: {
		label: {
			full_name: 'Full Name',
			email: 'Email',
			password: 'Password',
			confirmation_password: 'Confirmation Password',
			starttls: 'StartTLS',
			webhook_url: 'Webhook URL',
			name: 'Name {{ entity }}',
			description: 'Description',
			hostname: 'Hostname',
			port: 'Port',
			username: 'Username',
			address_from: 'Address From',
			address_to: 'Address To',
			host: 'Host',
			url: 'Url',
			enabled: 'Enabled',
			method: 'Method',
		},
		placeholder: {
			email: 'john.doe@exemple.com',
			password: '********',
			mailer_url: 'smtp.exemple.com',
		},
		description: {
			password: 'Must be at least 8 characters long',
		},
	},

	button: {
		login: 'Login',
		retry: 'Try again',
		close: 'Close',
		cancel: 'Cancel',
		loading: 'Logging...',
		exporting: 'Exporting...',
		create: 'Create {{entity}}',
		update: 'Update {{entity}}',
		remove: 'Remove {{entity}}',
		purge: 'Purge',
		purging: 'Purging...',
		saving: 'Saving...',
		save: 'Save {{entity}}',
		test: 'Test {{entity}}',

		actions: {
			pause: 'Pause',
			resume: 'Resume',
			remove: 'Remove',
			edit: 'Edit',
			export_csv: 'Export CSV',
			purge_logs: 'Purge logs',
		},
	},

	entity: {
		monitor: 'monitor',
		notification: 'notification',
		profile: 'profile',
	},

	pages: {
		status_page: {
			title: 'Health Dashboard',
			subtitle: 'Real-time infrastructure monitoring',
			description: {
				last_update: 'Last update: ',
				automatic_refresh: 'Auto-refresh',
			},
			uptime_30d: '30-day uptime',
			down_for: 'Down for {{duration}}',
			verdict: {
				operational_one: 'All systems operational',
				operational_other: 'All {{count}} services are operational',
				degraded_one: '1 service of {{total}} is degraded',
				degraded_other: '{{count}} services of {{total}} are degraded',
				down_one: '1 service of {{total}} is experiencing an incident',
				down_other: '{{count}} services of {{total}} are experiencing an incident',
			},
			empty: {
				title: 'No monitor yet',
				description: 'Add a monitor to start watching your infrastructure.',
			},
		},
		login: {
			title: 'Login to your account',
			description: 'Please enter your credentials to log in.',
		},
	},

	notifications: {
		title: {
			create: 'Create notification',
			update: 'Update notification',
			remove: 'Remove notification',
			notifications: 'Notifications',
		},
		description: {
			settings: 'Manage your notifications',
			remove: 'This action is irreversible. All associated data will be permanently deleted',
		},
		empty: {
			title: 'No notification channel yet',
			description: 'Create one to be alerted when a monitor goes down.',
		},
		label: {
			is_default: 'Default',
			type_notification: 'Type notification',
			notification_name: 'Notification name',
		},
		placeholder: {
			notification_name: 'Discord bot',
		},
		alerts: {
			create: 'Notification {{ data }} successfully created',
			update: 'Notification {{ data }} successfully updated',
			remove: 'Notification successfully removed',
			testing: {
				success: 'Notification successfully tested',
				error: 'Error while testing notification',
			},
		},
	},

	monitors: {
		protocol_group: {
			network: 'Web & network',
			database: 'Databases',
			messaging: 'Messaging & mail',
		},

		section: {
			target: 'What to monitor',
			schedule: 'Schedule',
			schedule_description: 'How often the probe runs, and how it retries before raising an alert.',
			settings: 'Settings',
			notifications_description: 'Channels alerted when this monitor goes down.',
		},

		title: {
			create: 'Create monitor',
			update: 'Update monitor',
			remove: 'Remove monitor',
			purge_logs: 'Purge monitor logs',
			pause: 'Pause this monitor?',
			resume: 'Resume this monitor?',
			final_hour: 'Last hour',
			http_request_assertions: 'HTTP request and assertions',
		},
		label: {
			http_method: 'HTTP method',
			follow_redirects: 'Follow redirects',
			max_latency_ms: 'Maximum latency (ms)',
			tls_expiry_warning_days: 'TLS expiry warning threshold (days)',
			request_body: 'Request body',
			request_headers_json: 'Request headers (JSON object)',
			authentication: 'Authentication',
			assertions_json: 'Assertions (JSON array)',
			scenario_steps_json: 'Scenario steps (JSON array, optional)',
			tcp_host: 'TCP host',
			tcp_port: 'TCP port',
			dns_server: 'DNS server',
			dns_port: 'DNS port',
			notification_certificate: 'Certificate expiry notification',
			ignore_certificate_errors: 'Ignore TLS / SSL errors for HTTS websites',
			http_code_allowed: 'Accepted Status Codes',
			dns_record: 'DNS record',
			ping_heartbeat_interval: 'Heartbeat interval',
			ping_max_packet: 'Max packet loss',
			ping_size: 'Packet size',
			ping_delay: 'Delay between pings (ms)',
			postgresql_connection_string: 'PostgreSQL connection string',
			postgresql_query: 'Query',
			sqlserver_connection_string: 'Microsoft SQL Server connection string',
			sqlserver_query: 'Query',
			mysql_connection_string: 'MySQL/MariaDB connection string',
			mysql_query: 'Query',
			redis_connection_string: 'Redis connection string',
			redis_command: 'Command',
			smtp_hostname: 'Hostname / IP address',
			smtp_port: 'Port',
			smtp_security: 'SMTP security',
			kafka_brokers: 'Kafka brokers',
			kafka_topic: 'Kafka topic name',
			kafka_message: 'Kafka producer message',
			kafka_ssl: 'Enable Kafka SSL',
			kafka_auto_topic_creation: 'Enable Kafka automatic topic creation',
			rabbitmq_management_nodes: 'RabbitMQ management nodes',
			rabbitmq_username: 'RabbitMQ username',
			rabbitmq_password: 'RabbitMQ password',
			protocol: 'Monitor protocol',
			interval: 'Check interval (s)',
			name_monitor: 'Monitor name',
			retry: 'Retries',
			interval_retry: 'Retry interval (s)',
		},
		uptime: {
			h24: 'Uptime 24h',
			d7: 'Uptime 7d',
			d30: 'Uptime 30d',
		},
		latency: {
			current: 'Current',
			average: 'Average',
			max_peak: 'Max peak',
			min: 'Min',
		},
		chart: {
			title: 'Response time',
			description: 'Showing probe response times for {{range}}',
			response_time: 'Response time (ms)',
			average_reference: 'avg {{value}} ms',
			select_range: 'Select time range',
			loading: 'Loading chart…',
			empty: 'No data available for the selected time range',
		},
		logs: {
			title: 'Monitoring logs',
			description: 'Recent monitor activity',
			filter_all: 'All',
			filter_success: 'Success',
			filter_errors: 'Errors',
			no_message: 'No message',
			empty_filter: 'No logs for this filter',
			showing: 'Showing {{count}} of {{total}} — scroll for more',
		},
		description: {
			authentication_optional: 'Optional. Select Basic or Bearer only when required.',
			max_latency_ms: 'The check fails when the response is slower. Between 1 and 5000 ms, since the HTTP client times out after 5 s. Leave empty to disable the threshold.',
			remove: 'This action is irreversible. All associated data will be permanently deleted',
			purge_logs: 'This will permanently delete the full history for this monitor.',
			dns_server: 'Cloudflare is the default server. You can change the resolver server anytime.',
			dns_port: 'DNS server port. Defaults to 53. You can change the port at any time.',
			smtp_security:
				"'SMTPS' tests implicit SMTP/TLS; 'Ignore TLS' connects in plain text; 'STARTTLS' connects, sends STARTTLS and verifies the server certificate. None of these checks sends an email.",
			rabbitmq_management_nodes:
				'Enter RabbitMQ management node URLs including protocol and port, separated by commas. Example: https://node1.rabbitmq.com:15672',
			internal_retry: 'Maximum retries before the service is marked as down and a notification is sent',
			check_interval: 'Check every {{ interval }} seconds',
			enabled: 'A paused monitor keeps its history but runs no check.',
			now: 'Now',
			one_hour_ago: '1 hour ago',
			paused_slot: 'The monitor was paused, no check was run.',
			pause: 'Checks stop until you resume it. The history is kept, and no alert will be sent.',
			resume: 'Checks restart immediately, on the configured interval.',
		},
		option: {
			smtp_ignore_tls: 'Ignore TLS',
			no_authentication: 'No authentication',
			basic_authentication: 'Basic authentication',
			bearer_token: 'Bearer token',
		},
		placeholder: {
			name_monitor: 'Production API',
			username: 'Username',
			password: 'Password',
			bearer_token: 'Bearer token',
		},
		error: {
			invalid_json: 'Invalid JSON',
		},
		alerts: {
			create: 'Monitor {{ data }} successfully created',
			update: 'Monitor {{ data }} successfully updated',
			remove: 'Monitor successfully removed',
			purge_logs: 'Monitor logs purged successfully',
			paused: 'Monitor paused',
			resumed: 'Monitor resumed',
		},
	},

	profile: {
		title: {
			update_profile: 'Update profile',
			update_password: 'Update password',
			create_first_user: 'Create your account',
		},
		tabs: {
			account: 'Account',
			password: 'Password',
			notifications: 'Notifications',
		},
		description: {
			current_password: 'For security reasons, confirm the password you use today.',
			update_password: 'Changing your password will log you out of all your sessions. You will need to log in again.',
			update_profile: 'Manage your personal information to keep your account accurate and secure.',
			create_first_user: 'Enter your email address to create the first user and initialize the application.',
		},
		label: {
			current_password: 'Current Password',
			password: 'New Password',
			password_confirm: 'Confirmation Password',
		},
		alerts: {
			update_profile: 'Profile successfully updated',
			update_password: 'Password successfully updated',
		},
	},

	dashboard: {
		title: {
			monitors: 'Monitors',
			monitors_up: 'Monitors UP',
			monitors_down: 'Monitors DOWN',
			uptime: 'Uptime average',
			notifications: 'Notifications',
			last_24_hours: 'Last 24 hours',
			response_time_average: 'Response time average',
			incidents: 'Incidents',
			checks_executed: 'Checks executed',
		},

		description: {
			monitors: 'Total monitors',
			monitors_up: 'Functional',
			monitors_down: 'Down',
			uptime: 'All services',
			avg_response_time: 'Average latency',
			on_24_hours: 'On 24 hours',
			latency_average: 'Average latency',
			executing: 'Executed',
			currently_incidents: 'Currently active incidents',
		},

		events: {
			title: 'Recent events',
			description: 'State changes across your monitors',
			empty: 'No state change in the last 7 days',
		},

		legend: {
			up: 'Up',
			down: 'Down',
		},

		table: {
			services: 'Services',
			times: 'Times',
			status: 'Status',
			empty: 'No data available in the table',
		},
	},

	timeRanger: {
		last_1_hour: 'Last 1 hour',
		last_3_hours: 'Last 3 hours',
		last_6_hours: 'Last 6 hours',
		last_24_hours: 'Last 24 hours',
		last_7_days: 'Last 7 days',
	},

	layout: {
		theme: {
			switch_to_light: 'Switch to light theme',
			switch_to_dark: 'Switch to dark theme',
		},
		updated: {
			seconds_one: 'Updated {{count}}s ago',
			seconds_other: 'Updated {{count}}s ago',
			minutes_one: 'Updated {{count}} min ago',
			minutes_other: 'Updated {{count}} min ago',
		},
		sidebar: {
			monitors: 'Monitors',
			settings: 'Settings',
			logout: 'Logout',
			dashboard: 'Dashboard',
			status_page: 'Status page',
			new_monitor: 'New monitor',
		},
	},
};

export default en;
