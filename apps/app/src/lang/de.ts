import type en from '@/lang/en.ts';

// Typisiert auf `en`: ein fehlender oder überzähliger Schlüssel ist ein Compile-Fehler.
const de: typeof en = {
	errors: {
		load: {
			title: 'Diese Daten konnten nicht geladen werden',
			description:
				'Die API hat nicht geantwortet. Sie startet möglicherweise neu oder ist vom Browser aus nicht erreichbar.',
		},
	},

	app: {
		loading: 'Uptime Kotlin wird geladen',
		loading_short: 'Wird geladen…',
	},

	status: {
		healthy: 'In Betrieb',
		degraded: 'Beeinträchtigt',
		unhealthy: 'Ausgefallen',
		paused: 'Pausiert',
	},

	form: {
		label: {
			full_name: 'Vollständiger Name',
			email: 'E-Mail-Adresse',
			password: 'Passwort',
			confirmation_password: 'Passwort bestätigen',
			starttls: 'StartTLS',
			webhook_url: 'Webhook-URL',
			name: 'Name {{ entity }}',
			description: 'Beschreibung',
			hostname: 'Hostname',
			port: 'Port',
			username: 'Benutzername',
			address_from: 'Absender',
			address_to: 'Empfänger',
			host: 'Host',
			url: 'URL',
			enabled: 'Aktiviert',
			method: 'Methode',
			bot_name: 'Bot-Name',
		},
		placeholder: {
			email: 'max.mustermann@beispiel.de',
			password: '********',
			mailer_url: 'smtp.beispiel.de',
			full_name: 'Max Mustermann',
			admin_email: 'admin@uptime-kotlin.com',
			bot_name: 'Uptime Kotlin',
		},
		description: {
			password: 'Mindestens 8 Zeichen',
		},
	},

	button: {
		login: 'Anmelden',
		retry: 'Erneut versuchen',
		close: 'Schließen',
		cancel: 'Abbrechen',
		loading: 'Anmeldung…',
		exporting: 'Export läuft…',
		create: '{{entity}} erstellen',
		update: '{{entity}} bearbeiten',
		remove: '{{entity}} löschen',
		purge: 'Bereinigen',
		removing: 'Wird gelöscht…',
		purging: 'Wird bereinigt…',
		saving: 'Wird gespeichert…',
		save: '{{entity}} speichern',
		test: '{{entity}} testen',

		actions: {
			pause: 'Pausieren',
			resume: 'Fortsetzen',
			remove: 'Löschen',
			edit: 'Bearbeiten',
			export_csv: 'CSV exportieren',
			more: 'Weitere Aktionen',
			check_now: 'Jetzt prüfen',
			purge_logs: 'Protokolle bereinigen',
		},
	},

	entity: {
		monitor: 'Monitor',
		notification: 'Benachrichtigung',
		profile: 'Profil',
		maintenance: 'Wartungsfenster',
		first_account: 'Erstkonto',
	},

	validation: {
		passwords_mismatch: 'Die Passwörter stimmen nicht überein',
		password_must_differ: 'Das neue Passwort muss sich vom aktuellen unterscheiden',
		password_required: 'Passwort ist erforderlich',
		alert_repeat_seconds: '0 zum Deaktivieren, sonst mindestens 60 Sekunden',
		recurrence_until_after_start: 'Die Wiederholung muss nach ihrem ersten Auftreten enden',
	},

	select: {
		placeholder: 'Auswählen',
		empty: 'Keine Treffer.',
	},

	pages: {
		status_page: {
			title: 'Statusübersicht',
			subtitle: 'Infrastrukturüberwachung in Echtzeit',
			description: {
				last_update: 'Zuletzt aktualisiert: ',
				automatic_refresh: 'Automatische Aktualisierung',
			},
			uptime_30d: 'Verfügbarkeit über 30 Tage',
			down_for: 'Ausgefallen seit {{duration}}',
			planned_downtime: '{{duration}} geplant',
			verdict: {
				operational_one: 'Alle Systeme in Betrieb',
				operational_other: 'Alle {{count}} Dienste sind in Betrieb',
				degraded_one: '1 von {{total}} Diensten ist beeinträchtigt',
				degraded_other: '{{count}} von {{total}} Diensten sind beeinträchtigt',
				down_one: '1 von {{total}} Diensten hat eine Störung',
				down_other: '{{count}} von {{total}} Diensten haben eine Störung',
				maintenance_one: '1 Dienst in Wartung',
				maintenance_other: '{{count}} Dienste in Wartung',
			},
			empty: {
				title: 'Noch kein Monitor',
				description: 'Legen Sie einen Monitor an, um Ihre Infrastruktur zu überwachen.',
			},
		},
		login: {
			title: 'Bei Ihrem Konto anmelden',
			description: 'Bitte geben Sie Ihre Zugangsdaten ein.',
		},
	},

	notifications: {
		title: {
			create: 'Benachrichtigung erstellen',
			update: 'Benachrichtigung bearbeiten',
			remove: 'Benachrichtigung löschen',
			notifications: 'Benachrichtigungen',
		},
		description: {
			settings: 'Verwalten Sie Ihre Benachrichtigungen',
			remove: 'Dieser Vorgang ist unwiderruflich. Alle zugehörigen Daten werden endgültig gelöscht',
		},
		empty: {
			title: 'Noch kein Benachrichtigungskanal',
			description: 'Legen Sie einen an, um bei einem Ausfall benachrichtigt zu werden.',
		},
		label: {
			is_default: 'Standard',
			set_as_default: 'Diese Benachrichtigung als Standard verwenden',
			type_notification: 'Art der Benachrichtigung',
			notification_name: 'Name der Benachrichtigung',
		},
		placeholder: {
			notification_name: 'Discord-Bot',
		},
		alerts: {
			create: 'Benachrichtigung {{ data }} erstellt',
			update: 'Benachrichtigung {{ data }} aktualisiert',
			remove: 'Benachrichtigung gelöscht',
			testing: {
				success: 'Benachrichtigung erfolgreich getestet',
				error: 'Fehler beim Test der Benachrichtigung',
			},
		},
	},

	monitors: {
		protocol_group: {
			network: 'Web und Netzwerk',
			database: 'Datenbanken',
			messaging: 'Messaging und E-Mail',
		},

		section: {
			target: 'Was überwacht wird',
			schedule: 'Zeitplan',
			schedule_description:
				'Wie oft die Prüfung läuft und wie oft sie wiederholt wird, bevor ein Alarm ausgelöst wird.',
			settings: 'Einstellungen',
			notifications_description: 'Kanäle, die bei einem Ausfall dieses Monitors benachrichtigt werden.',
		},

		title: {
			create: 'Monitor erstellen',
			update: 'Monitor bearbeiten',
			remove: 'Monitor löschen',
			purge_logs: 'Monitor-Protokolle bereinigen',
			pause: 'Diesen Monitor pausieren?',
			resume: 'Diesen Monitor fortsetzen?',
			final_hour: 'Letzte Stunde',
			http_request_assertions: 'HTTP-Anfrage und Prüfungen',
		},
		label: {
			http_method: 'HTTP-Methode',
			follow_redirects: 'Weiterleitungen folgen',
			max_latency_ms: 'Maximale Latenz (ms)',
			tls_expiry_warning_days: 'Warnschwelle für TLS-Ablauf (Tage)',
			request_body: 'Anfrage-Body',
			request_headers_json: 'Anfrage-Header (JSON-Objekt)',
			authentication: 'Authentifizierung',
			assertions_json: 'Prüfungen (JSON-Array)',
			scenario_steps_json: 'Szenario-Schritte (JSON-Array, optional)',
			tcp_host: 'TCP-Host',
			tcp_port: 'TCP-Port',
			dns_server: 'DNS-Server',
			dns_port: 'DNS-Port',
			notification_certificate: 'Benachrichtigung bei Zertifikatsablauf',
			ignore_certificate_errors: 'TLS-/SSL-Fehler bei HTTPS-Seiten ignorieren',
			http_code_allowed: 'Akzeptierte Statuscodes',
			dns_record: 'DNS-Eintrag',
			ping_heartbeat_interval: 'Heartbeat-Intervall',
			ping_max_packet: 'Maximaler Paketverlust',
			ping_size: 'Paketgröße',
			ping_delay: 'Abstand zwischen zwei Pings (ms)',
			postgresql_connection_string: 'PostgreSQL-Verbindungszeichenfolge',
			postgresql_query: 'Abfrage',
			sqlserver_connection_string: 'Microsoft-SQL-Server-Verbindungszeichenfolge',
			sqlserver_query: 'Abfrage',
			mysql_connection_string: 'MySQL-/MariaDB-Verbindungszeichenfolge',
			mysql_query: 'Abfrage',
			redis_connection_string: 'Redis-Verbindungszeichenfolge',
			redis_command: 'Befehl',
			smtp_hostname: 'Hostname / IP-Adresse',
			smtp_port: 'Port',
			smtp_security: 'SMTP-Sicherheit',
			kafka_brokers: 'Kafka-Broker',
			kafka_topic: 'Name des Kafka-Topics',
			kafka_message: 'Nachricht des Kafka-Producers',
			kafka_ssl: 'SSL für Kafka aktivieren',
			kafka_auto_topic_creation: 'Automatische Topic-Erstellung in Kafka aktivieren',
			rabbitmq_management_nodes: 'RabbitMQ-Management-Knoten',
			rabbitmq_username: 'RabbitMQ-Benutzername',
			rabbitmq_password: 'RabbitMQ-Passwort',
			protocol: 'Protokoll des Monitors',
			interval: 'Prüfintervall (s)',
			name_monitor: 'Name des Monitors',
			retry: 'Wiederholungen',
			interval_retry: 'Abstand zwischen Wiederholungen (s)',
			alert_repeat_seconds: 'Alarm erneut senden alle (s)',
			remove_confirmation: 'Namen des Monitors bestätigen',
		},
		tls: {
			expired: 'Zertifikat vor {{days}} T abgelaufen',
			expires_in: 'Zertifikat läuft in {{days}} T ab',
			expires_today: 'Zertifikat läuft heute ab',
			valid_for: 'Zertifikat noch {{days}} T gültig',
			expires_on: 'Läuft ab am {{date}}',
			checked_at: 'Zuletzt gelesen {{date}}',
		},
		uptime: {
			h24: 'Verfügbarkeit 24 Std.',
			d7: 'Verfügbarkeit 7 T',
			d30: 'Verfügbarkeit 30 T',
		},
		latency: {
			current: 'Aktuell',
			average: 'Durchschnitt',
			max_peak: 'Höchstwert',
			min: 'Minimum',
		},
		chart: {
			title: 'Antwortzeit',
			description: 'Antwortzeiten der Prüfungen über {{range}}',
			response_time: 'Antwortzeit (ms)',
			average_reference: 'Ø {{value}} ms',
			select_range: 'Zeitraum wählen',
			loading: 'Diagramm wird geladen…',
			empty: 'Keine Daten im gewählten Zeitraum',
		},
		logs: {
			title: 'Überwachungsprotokolle',
			description: 'Jüngste Aktivität des Monitors',
			filter_all: 'Alle',
			filter_success: 'Erfolge',
			filter_errors: 'Fehler',
			no_message: 'Keine Meldung',
			empty_filter: 'Keine Protokolle für diesen Filter',
			showing: '{{count}} von {{total}} angezeigt — weiter scrollen',
		},
		description: {
			authentication_optional: 'Optional. Basic oder Bearer nur wählen, wenn es erforderlich ist.',
			remove_confirmation: 'Geben Sie genau diesen Wert ein, um das Löschen freizugeben:',
			max_latency_ms:
				'Die Prüfung schlägt fehl, wenn die Antwort langsamer ist. Zwischen 1 und 5000 ms, da der HTTP-Client nach 5 s abbricht. Leer lassen, um die Schwelle zu deaktivieren.',
			remove: 'Dieser Vorgang ist unwiderruflich. Alle zugehörigen Daten werden endgültig gelöscht',
			check_now_disabled: 'Setzen Sie diesen Monitor fort, um eine Prüfung zu starten.',
			purge_logs: 'Dies löscht die gesamte Historie dieses Monitors endgültig.',
			dns_server: 'Cloudflare ist der Standardserver. Sie können den Resolver jederzeit wechseln.',
			dns_port: 'Port des DNS-Servers. Standard ist 53. Jederzeit änderbar.',
			smtp_security:
				'„SMTPS“ prüft implizites SMTP/TLS; „TLS ignorieren“ verbindet im Klartext; „STARTTLS“ verbindet, sendet STARTTLS und prüft das Serverzertifikat. Keine dieser Prüfungen versendet eine E-Mail.',
			rabbitmq_management_nodes:
				'Geben Sie die URLs der RabbitMQ-Management-Knoten samt Protokoll und Port an, durch Kommas getrennt. Beispiel: https://node1.rabbitmq.com:15672',
			internal_retry:
				'Anzahl der Wiederholungen, bevor der Dienst als ausgefallen gilt und eine Benachrichtigung ausgeht',
			alert_repeat_seconds:
				'Solange der Monitor ausgefallen bleibt, wird der Alarm in diesem Abstand erneut über dieselben Kanäle gesendet. 0 sendet wie bisher nur eine einzige Benachrichtigung. Minimum 60 s.',
			check_interval: 'Prüfung alle {{ interval }} Sekunden',
			enabled: 'Ein pausierter Monitor behält seine Historie, führt aber keine Prüfung aus.',
			now: 'Jetzt',
			one_hour_ago: 'Vor 1 Stunde',
			paused_slot: 'Der Monitor war pausiert, es wurde keine Prüfung ausgeführt.',
			pause: 'Die Prüfungen ruhen bis zur Fortsetzung. Die Historie bleibt erhalten, es wird kein Alarm gesendet.',
			resume: 'Die Prüfungen starten sofort wieder, im konfigurierten Intervall.',
		},
		option: {
			smtp_ignore_tls: 'TLS ignorieren',
			no_authentication: 'Keine Authentifizierung',
			basic_authentication: 'Basic-Authentifizierung',
			bearer_token: 'Bearer-Token',
		},
		placeholder: {
			name_monitor: 'Produktions-API',
			username: 'Benutzername',
			password: 'Passwort',
			bearer_token: 'Bearer-Token',
		},
		error: {
			invalid_json: 'Ungültiges JSON',
		},
		alerts: {
			create: 'Monitor {{ data }} erstellt',
			update: 'Monitor {{ data }} aktualisiert',
			remove: 'Monitor gelöscht',
			purge_logs: 'Monitor-Protokolle bereinigt',
			check_now: 'Prüfung angefordert, das Ergebnis erscheint in Kürze',
			check_now_already_running: 'Für diesen Monitor läuft bereits eine Prüfung',
			paused: 'Monitor pausiert',
			resumed: 'Monitor fortgesetzt',
		},
	},

	maintenances: {
		title: {
			index: 'Wartungsfenster',
			start: 'Wartungsfenster starten',
			remove: 'Dieses Wartungsfenster löschen?',
		},
		description: {
			index: 'Geplante Ausfallzeit: Alarme bleiben stumm und die Verfügbarkeit wird nicht belastet.',
			start:
				'Alarme werden unterdrückt und die Verfügbarkeit wird nicht belastet. Die Prüfungen laufen weiter, Sie sehen den Dienst also zurückkommen.',
			start_hint: 'Sie können jederzeit vorzeitig beenden. Dass es keine unbefristete Option gibt, ist Absicht.',
			remove: 'Mit „{{title}}“ werden auch die geplanten Wiederholungen gelöscht. Alarme laufen sofort wieder.',
		},
		actions: {
			schedule: 'Fenster planen',
			start: 'Wartung',
			end: 'Jetzt beenden',
		},
		badge: {
			in_progress: 'Wartung, noch {{duration}}',
			scheduled: 'Wartung in {{duration}}',
		},
		recurrence: {
			ONCE: 'Einmalig',
			DAILY: 'Täglich',
			WEEKLY: 'Wöchentlich',
			MONTHLY: 'Monatlich',
		},
		table: {
			title: 'Fenster',
			state: 'Nächster Termin',
			recurrence: 'Wiederholung',
			duration: 'Dauer',
			monitors: 'Monitore',
			actions: 'Aktionen',
			no_occurrence: 'Nichts geplant',
			inactive: 'Deaktiviert',
		},
		form: {
			title_placeholder: 'Datenbankmigration',
			probes: 'Abgedeckte Monitore',
			probes_description: 'Ein Fenster ohne Monitor bringt nichts zum Schweigen.',
			starts_at: 'Beginn',
			duration: 'Dauer (Minuten)',
			duration_description: 'Ein Fenster, das niemand schließt, ist eine stillschweigend abgeschaltete Überwachung.',
			recurrence: 'Wiederholung',
			timezone: 'Zeitzone',
			timezone_description: 'Hält ein 02:00-Fenster auch über eine Zeitumstellung hinweg bei 02:00.',
			recurrence_until: 'Wiederholen bis',
			recurrence_until_description: 'Leer lassen, um unbegrenzt zu wiederholen.',
			active: 'Aktiviert',
			active_description: 'Deaktivieren Sie es, um das Fenster zu behalten, ohne dass es etwas unterdrückt.',
			is_public: 'Auf der Statusseite anzeigen',
			is_public_description:
				'Wer den Zeitplan einer internen Umgebung veröffentlicht, veröffentlicht auch, wann ihre Abwehr unten ist.',
		},
		alerts: {
			created: 'Wartungsfenster „{{title}}“ geplant',
			updated: 'Wartungsfenster „{{title}}“ aktualisiert',
			removed: 'Wartungsfenster gelöscht',
			started: 'Wartung gestartet',
			ended: 'Wartung beendet',
			occurrence_cancelled: 'Termin abgesagt',
		},
		empty: {
			title: 'Noch kein Wartungsfenster',
			description: 'Planen Sie eines vor Ihrem nächsten Deployment, dann bleiben die Alarme stumm.',
		},
	},

	profile: {
		title: {
			update_profile: 'Profil bearbeiten',
			update_password: 'Passwort ändern',
			create_first_user: 'Konto anlegen',
			preferences: 'Anzeigeeinstellungen',
		},
		tabs: {
			account: 'Konto',
			password: 'Passwort',
			notifications: 'Benachrichtigungen',
			preferences: 'Darstellung',
		},
		description: {
			current_password: 'Bestätigen Sie aus Sicherheitsgründen Ihr aktuelles Passwort.',
			update_password: 'Eine Passwortänderung meldet Sie von allen Sitzungen ab. Sie müssen sich neu anmelden.',
			update_profile: 'Pflegen Sie Ihre persönlichen Daten, damit Ihr Konto korrekt und sicher bleibt.',
			create_first_user:
				'Geben Sie Ihre E-Mail-Adresse ein, um den ersten Benutzer anzulegen und die Anwendung einzurichten.',
			preferences: 'In diesem Browser gespeichert, nicht im Konto: ein anderer Browser behält seine eigene Wahl.',
			language: 'Wirkt sofort. Datum, Uhrzeit und Zahlen folgen derselben Wahl.',
			theme: '„System“ folgt der Einstellung Ihres Betriebssystems.',
		},
		label: {
			current_password: 'Aktuelles Passwort',
			password: 'Neues Passwort',
			password_confirm: 'Passwort bestätigen',
			language: 'Sprache',
			theme: 'Erscheinungsbild',
		},
		alerts: {
			update_profile: 'Profil aktualisiert',
			update_password: 'Passwort aktualisiert',
			create_first_user: 'Erster Benutzer angelegt',
		},
	},

	dashboard: {
		title: {
			monitors: 'Monitore',
			monitors_up: 'Monitore UP',
			monitors_down: 'Monitore DOWN',
			uptime: 'Durchschnittliche Verfügbarkeit',
			notifications: 'Benachrichtigungen',
			last_24_hours: 'Letzte 24 Stunden',
			response_time_average: 'Durchschnittliche Antwortzeit',
			incidents: 'Störungen',
			checks_executed: 'Ausgeführte Prüfungen',
		},

		description: {
			monitors: 'Monitore insgesamt',
			monitors_up: 'In Betrieb',
			monitors_down: 'Ausgefallen',
			uptime: 'Über alle Dienste',
			avg_response_time: 'Durchschnittliche Latenz',
			on_24_hours: 'Über 24 Stunden',
			latency_average: 'Durchschnittliche Latenz',
			executing: 'Ausgeführt',
			currently_incidents: 'Aktuell laufende Störungen',
		},

		events: {
			title: 'Jüngste Ereignisse',
			description: 'Statuswechsel Ihrer Monitore',
			empty: 'Kein Statuswechsel in den letzten 7 Tagen',
		},

		legend: {
			up: 'In Betrieb',
			down: 'Ausgefallen',
		},

		table: {
			services: 'Dienste',
			times: 'Zeiten',
			status: 'Status',
			empty: 'Keine Daten in der Tabelle',
		},
	},

	timeRanger: {
		last_1_hour: 'Letzte Stunde',
		last_3_hours: 'Letzte 3 Stunden',
		last_6_hours: 'Letzte 6 Stunden',
		last_24_hours: 'Letzte 24 Stunden',
		last_7_days: 'Letzte 7 Tage',
	},

	layout: {
		breadcrumb: {
			monitor: 'Monitor',
		},
		theme: {
			switch_to_light: 'Zum hellen Erscheinungsbild wechseln',
			switch_to_dark: 'Zum dunklen Erscheinungsbild wechseln',
			system: 'System',
			light: 'Hell',
			dark: 'Dunkel',
		},
		language: {
			label: 'Sprache',
			en: 'English',
			fr: 'Français',
			de: 'Deutsch',
			es: 'Español',
		},
		updated: {
			seconds_one: 'Vor {{count}} s aktualisiert',
			seconds_other: 'Vor {{count}} s aktualisiert',
			minutes_one: 'Vor {{count}} Min. aktualisiert',
			minutes_other: 'Vor {{count}} Min. aktualisiert',
		},
		sidebar: {
			monitors: 'Monitore',
			search: {
				placeholder: 'Monitore filtern',
				label: 'Monitore nach Name oder Beschreibung filtern',
				clear: 'Filter zurücksetzen',
				count: '{{visible}} von {{total}}',
				empty: 'Kein Monitor passt zu „{{query}}“.',
			},
			settings: 'Einstellungen',
			logout: 'Abmelden',
			dashboard: 'Übersicht',
			status_page: 'Statusseite',
			maintenances: 'Wartung',
			new_monitor: 'Neuer Monitor',
		},
	},
};

export default de;
