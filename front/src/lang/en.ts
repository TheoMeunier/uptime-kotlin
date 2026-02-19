const en = {
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
		close: 'Close',
		cancel: 'Cancel',
		loading: 'Logging...',
		create: 'Create {{entity}}',
		update: 'Update {{entity}}',
		remove: 'Remove {{entity}}',
		saving: 'Saving...',
		save: 'Save {{entity}}',
		test: 'Test {{entity}}',

		actions: {
			remove: 'Remove',
			edit: 'Edit',
		},
	},

	entity: {
		monitor: 'monitor',
		notification: 'notification',
		profile: 'profile',
	},

	pages: {
		status_page: {
			title: 'Dashboard',
			subtitle: 'Real-time infrastructure monitoring',
			description: {
				last_update: 'Last update: ',
				automatic_refresh: 'Auto-refresh',
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
		label: {
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
		title: {
			create: 'Create monitor',
			remove: 'Remove monitor',
			final_hour: 'Final hour',
		},
		label: {
			tcp_host: 'TCP host',
			tcp_port: 'TCP port',
			dns_server: 'DNS server',
			dns_port: 'DNS port',
			notification_certificate: 'Certificate expiry notification',
			ignore_certificate_errors: 'Ignore TLS / SSL errors for HTTS websites',
			http_code_allowed: 'Accepted Status Codes',
			ping_heartbeat_interval: 'Heartbeat interval',
			ping_max_packet: 'Max packet loss',
			ping_size: 'Packet size',
			ping_delay: 'Delay between pings (ms)',
			protocol: 'Monitor protocol',
			name_monitor: 'Monitor name',
			retry: 'Retry',
			interval_retry: 'Retry interval',
		},
		description: {
			remove: 'This action is irreversible. All All associated data will be permanently deleted',
			dns_server: 'Cloudflare is the default server. You can change the resolver server anytime.',
			dns_port: 'DNS server port. Defaults to 53. You can change the port at any time.',
			internal_retry: 'Maximum retries before the service is marked as down and a notification is sent',
			check_interval: 'Check every {{ interval }} secondes',
			now: 'Now',
			one_hour_ago: '1 hour ago',
		},
		alerts: {
			create: 'Monitor {{ data }} successfully created',
			update: 'Monitor {{ data }} successfully updated',
			remove: 'Monitor successfully removed',
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
			update_password: 'Changing your password will log you out of all your sessions. You will need to log in again.',
			update_profile: 'Manage your personal information to keep your account accurate and secure.',
			create_first_user: 'Enter your email address to create the first user and initialize the application.',
		},
		label: {
			password: 'Password',
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
		sidebar: {
			settings: 'Settings',
			logout: 'Logout',
			dashboard: 'Dashboard',
			status_page: 'Status page',
			new_monitor: 'New monitor',
		},
	},
};

export default en;
