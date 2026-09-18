import type en from '@/lang/en.ts';

// Typé sur `en` : une clé manquante ou en trop est une erreur de compilation.
const fr: typeof en = {
	errors: {
		load: {
			title: 'Impossible de charger ces données',
			description: 'L’API n’a pas répondu. Elle redémarre peut-être, ou elle est injoignable depuis votre navigateur.',
		},
	},

	unsaved_changes: {
		title: 'Quitter sans enregistrer ?',
		description:
			'Vos modifications de ce formulaire n’ont pas été enregistrées. Elles seront perdues si vous quittez cette page.',
		stay: 'Continuer l’édition',
		leave: 'Quitter sans enregistrer',
	},

	app: {
		loading: 'Chargement d’Uptime Kotlin',
		loading_short: 'Chargement…',
	},

	status: {
		healthy: 'Opérationnel',
		degraded: 'Dégradé',
		unhealthy: 'En échec',
		paused: 'En pause',
	},

	form: {
		label: {
			full_name: 'Nom complet',
			email: 'Adresse e-mail',
			password: 'Mot de passe',
			confirmation_password: 'Confirmation du mot de passe',
			starttls: 'StartTLS',
			webhook_url: 'URL du webhook',
			name: 'Nom {{ entity }}',
			description: 'Description',
			hostname: 'Nom d’hôte',
			port: 'Port',
			username: 'Nom d’utilisateur',
			address_from: 'Expéditeur',
			address_to: 'Destinataire',
			host: 'Hôte',
			url: 'URL',
			enabled: 'Activé',
			method: 'Méthode',
			bot_name: 'Nom du bot',
		},
		placeholder: {
			email: 'jean.dupont@exemple.com',
			password: '********',
			mailer_url: 'smtp.exemple.com',
			full_name: 'Jean Dupont',
			admin_email: 'admin@uptime-kotlin.com',
			bot_name: 'Uptime Kotlin',
		},
		description: {
			password: 'Au moins 8 caractères',
		},
	},

	button: {
		login: 'Se connecter',
		retry: 'Réessayer',
		close: 'Fermer',
		cancel: 'Annuler',
		loading: 'Connexion…',
		exporting: 'Export en cours…',
		create: 'Créer {{entity}}',
		update: 'Modifier {{entity}}',
		remove: 'Supprimer {{entity}}',
		purge: 'Purger',
		removing: 'Suppression…',
		purging: 'Purge…',
		saving: 'Enregistrement…',
		save: 'Enregistrer {{entity}}',
		test: 'Tester {{entity}}',

		actions: {
			pause: 'Mettre en pause',
			resume: 'Reprendre',
			remove: 'Supprimer',
			edit: 'Modifier',
			export_csv: 'Exporter en CSV',
			more: 'Plus d’actions',
			check_now: 'Vérifier maintenant',
			purge_logs: 'Purger les journaux',
			duplicate: 'Dupliquer',
		},
	},

	entity: {
		monitor: 'la sonde',
		notification: 'la notification',
		profile: 'le profil',
		maintenance: 'la fenêtre de maintenance',
		first_account: 'le premier compte',
	},

	validation: {
		passwords_mismatch: 'Les mots de passe ne correspondent pas',
		password_must_differ: 'Le nouveau mot de passe doit être différent de l’actuel',
		password_required: 'Le mot de passe est obligatoire',
		alert_repeat_seconds: 'Utilisez 0 pour désactiver, ou au moins 60 secondes',
		recurrence_until_after_start: 'La récurrence doit se terminer après sa première occurrence',
	},

	select: {
		placeholder: 'Sélectionner',
		empty: 'Aucun résultat.',
	},

	pages: {
		status_page: {
			title: 'Tableau de santé',
			subtitle: 'Supervision de l’infrastructure en temps réel',
			description: {
				last_update: 'Dernière mise à jour : ',
				automatic_refresh: 'Rafraîchissement auto',
			},
			uptime_30d: 'Disponibilité sur 30 jours',
			down_for: 'Hors service depuis {{duration}}',
			planned_downtime: '{{duration}} planifiées',
			verdict: {
				operational_one: 'Tous les systèmes sont opérationnels',
				operational_other: 'Les {{count}} services sont opérationnels',
				degraded_one: '1 service sur {{total}} est dégradé',
				degraded_other: '{{count}} services sur {{total}} sont dégradés',
				down_one: '1 service sur {{total}} rencontre un incident',
				down_other: '{{count}} services sur {{total}} rencontrent un incident',
				maintenance_one: '1 service en maintenance',
				maintenance_other: '{{count}} services en maintenance',
			},
			empty: {
				title: 'Aucune sonde pour l’instant',
				description: 'Ajoutez une sonde pour commencer à surveiller votre infrastructure.',
			},
		},
		login: {
			title: 'Connexion à votre compte',
			description: 'Saisissez vos identifiants pour vous connecter.',
		},
	},

	notifications: {
		title: {
			create: 'Créer une notification',
			update: 'Modifier la notification',
			remove: 'Supprimer la notification',
			notifications: 'Notifications',
		},
		description: {
			settings: 'Gérez vos notifications',
			remove: 'Cette action est irréversible. Toutes les données associées seront définitivement supprimées',
		},
		empty: {
			title: 'Aucun canal de notification pour l’instant',
			description: 'Créez-en un pour être alerté quand une sonde tombe.',
		},
		label: {
			is_default: 'Par défaut',
			set_as_default: 'Utiliser cette notification par défaut',
			type_notification: 'Type de notification',
			notification_name: 'Nom de la notification',
		},
		placeholder: {
			notification_name: 'Bot Discord',
		},
		alerts: {
			create: 'Notification {{ data }} créée',
			update: 'Notification {{ data }} mise à jour',
			remove: 'Notification supprimée',
			testing: {
				success: 'Notification testée avec succès',
				error: 'Erreur lors du test de la notification',
			},
		},
	},

	monitors: {
		protocol_group: {
			network: 'Web et réseau',
			database: 'Bases de données',
			messaging: 'Messagerie et courriel',
		},

		section: {
			target: 'Quoi surveiller',
			schedule: 'Planification',
			schedule_description:
				'À quelle fréquence la sonde s’exécute, et comment elle réessaie avant de lever une alerte.',
			settings: 'Paramètres',
			notifications_description: 'Canaux alertés quand cette sonde tombe.',
		},

		duplicate: {
			copy_name: '{{name}} (copie)',
			description:
				'Copie de {{name}}. Ajustez la cible avant d’enregistrer : rien n’est créé tant que vous ne l’avez pas fait.',
		},

		title: {
			create: 'Créer une sonde',
			update: 'Modifier la sonde',
			remove: 'Supprimer la sonde',
			purge_logs: 'Purger les journaux de la sonde',
			pause: 'Mettre cette sonde en pause ?',
			resume: 'Reprendre cette sonde ?',
			final_hour: 'Dernière heure',
			http_request_assertions: 'Requête HTTP et assertions',
			duplicate: 'Dupliquer la sonde',
		},
		label: {
			http_method: 'Méthode HTTP',
			follow_redirects: 'Suivre les redirections',
			max_latency_ms: 'Latence maximale (ms)',
			tls_expiry_warning_days: 'Seuil d’alerte d’expiration TLS (jours)',
			request_body: 'Corps de la requête',
			request_headers_json: 'En-têtes de la requête (objet JSON)',
			authentication: 'Authentification',
			assertions_json: 'Assertions (tableau JSON)',
			scenario_steps_json: 'Étapes du scénario (tableau JSON, facultatif)',
			tcp_host: 'Hôte TCP',
			tcp_port: 'Port TCP',
			dns_server: 'Serveur DNS',
			dns_port: 'Port DNS',
			notification_certificate: 'Notification d’expiration du certificat',
			ignore_certificate_errors: 'Ignorer les erreurs TLS / SSL pour les sites HTTPS',
			http_code_allowed: 'Codes de statut acceptés',
			dns_record: 'Enregistrement DNS',
			ping_heartbeat_interval: 'Intervalle de battement',
			ping_max_packet: 'Perte de paquets maximale',
			ping_size: 'Taille des paquets',
			ping_delay: 'Délai entre deux pings (ms)',
			postgresql_connection_string: 'Chaîne de connexion PostgreSQL',
			postgresql_query: 'Requête',
			sqlserver_connection_string: 'Chaîne de connexion Microsoft SQL Server',
			sqlserver_query: 'Requête',
			mysql_connection_string: 'Chaîne de connexion MySQL/MariaDB',
			mysql_query: 'Requête',
			redis_connection_string: 'Chaîne de connexion Redis',
			redis_command: 'Commande',
			smtp_hostname: 'Nom d’hôte / adresse IP',
			smtp_port: 'Port',
			smtp_security: 'Sécurité SMTP',
			kafka_brokers: 'Brokers Kafka',
			kafka_topic: 'Nom du topic Kafka',
			kafka_message: 'Message du producteur Kafka',
			kafka_ssl: 'Activer SSL pour Kafka',
			kafka_auto_topic_creation: 'Activer la création automatique de topic Kafka',
			rabbitmq_management_nodes: 'Nœuds de management RabbitMQ',
			rabbitmq_username: 'Nom d’utilisateur RabbitMQ',
			rabbitmq_password: 'Mot de passe RabbitMQ',
			protocol: 'Protocole de la sonde',
			interval: 'Intervalle de vérification (s)',
			name_monitor: 'Nom de la sonde',
			retry: 'Tentatives',
			interval_retry: 'Intervalle entre les tentatives (s)',
			alert_repeat_seconds: 'Renvoyer l’alerte toutes les (s)',
			remove_confirmation: 'Confirmez le nom de la sonde',
		},
		tls: {
			expired: 'Certificat expiré depuis {{days}} j',
			expires_in: 'Certificat expirant dans {{days}} j',
			expires_today: 'Certificat expirant aujourd’hui',
			valid_for: 'Certificat valide {{days}} j',
			expires_on: 'Expire le {{date}}',
			checked_at: 'Dernière lecture {{date}}',
		},
		uptime: {
			h24: 'Disponibilité 24 h',
			d7: 'Disponibilité 7 j',
			d30: 'Disponibilité 30 j',
		},
		latency: {
			current: 'Actuelle',
			average: 'Moyenne',
			max_peak: 'Pic maximal',
			min: 'Minimale',
		},
		chart: {
			title: 'Temps de réponse',
			description: 'Temps de réponse des sondes sur {{range}}',
			response_time: 'Temps de réponse (ms)',
			average_reference: 'moy. {{value}} ms',
			select_range: 'Choisir la période',
			loading: 'Chargement du graphique…',
			empty: 'Aucune donnée sur la période sélectionnée',
		},
		logs: {
			title: 'Journaux de supervision',
			description: 'Activité récente de la sonde',
			filter_all: 'Tous',
			filter_success: 'Succès',
			filter_errors: 'Erreurs',
			no_message: 'Aucun message',
			empty_filter: 'Aucun journal pour ce filtre',
			showing: '{{count}} sur {{total}} affichés — faites défiler pour la suite',
		},
		description: {
			authentication_optional: 'Facultatif. Choisissez Basic ou Bearer uniquement si c’est nécessaire.',
			remove_confirmation: 'Saisissez exactement cette valeur pour autoriser la suppression :',
			max_latency_ms:
				'La vérification échoue si la réponse est plus lente. Entre 1 et 5000 ms, le client HTTP expirant au bout de 5 s. Laissez vide pour désactiver le seuil.',
			remove: 'Cette action est irréversible. Toutes les données associées seront définitivement supprimées',
			check_now_disabled: 'Reprenez cette sonde pour lancer une vérification.',
			purge_logs: 'Cela supprimera définitivement tout l’historique de cette sonde.',
			dns_server: 'Cloudflare est le serveur par défaut. Vous pouvez changer de résolveur à tout moment.',
			dns_port: 'Port du serveur DNS. 53 par défaut. Vous pouvez le changer à tout moment.',
			smtp_security:
				'« SMTPS » teste le SMTP/TLS implicite ; « Ignorer TLS » se connecte en clair ; « STARTTLS » se connecte, envoie STARTTLS et vérifie le certificat du serveur. Aucune de ces vérifications n’envoie de courriel.',
			rabbitmq_management_nodes:
				'Saisissez les URL des nœuds de management RabbitMQ, protocole et port compris, séparées par des virgules. Exemple : https://node1.rabbitmq.com:15672',
			internal_retry: 'Nombre de tentatives avant que le service soit marqué hors service et qu’une notification parte',
			alert_repeat_seconds:
				'Tant que la sonde reste hors service, l’alerte est rejouée sur les mêmes canaux à cet intervalle. 0 n’envoie qu’une seule notification, comme auparavant. Minimum 60 s.',
			check_interval: 'Vérification toutes les {{ interval }} secondes',
			enabled: 'Une sonde en pause conserve son historique mais n’exécute aucune vérification.',
			now: 'Maintenant',
			one_hour_ago: 'Il y a 1 heure',
			paused_slot: 'La sonde était en pause, aucune vérification n’a été exécutée.',
			pause: 'Les vérifications s’arrêtent jusqu’à la reprise. L’historique est conservé et aucune alerte ne partira.',
			resume: 'Les vérifications reprennent immédiatement, à l’intervalle configuré.',
		},
		option: {
			smtp_ignore_tls: 'Ignorer TLS',
			no_authentication: 'Aucune authentification',
			basic_authentication: 'Authentification Basic',
			bearer_token: 'Jeton Bearer',
		},
		placeholder: {
			name_monitor: 'API de production',
			username: 'Nom d’utilisateur',
			password: 'Mot de passe',
			bearer_token: 'Jeton Bearer',
		},
		error: {
			invalid_json: 'JSON invalide',
		},
		alerts: {
			create: 'Sonde {{ data }} créée',
			update: 'Sonde {{ data }} mise à jour',
			remove: 'Sonde supprimée',
			purge_logs: 'Journaux de la sonde purgés',
			check_now: 'Vérification demandée, le résultat arrive dans quelques secondes',
			check_now_already_running: 'Une vérification est déjà en cours pour cette sonde',
			paused: 'Sonde mise en pause',
			resumed: 'Sonde reprise',
		},
	},

	maintenances: {
		title: {
			index: 'Fenêtres de maintenance',
			start: 'Démarrer une fenêtre de maintenance',
			remove: 'Supprimer cette fenêtre de maintenance ?',
		},
		description: {
			index: 'Indisponibilité planifiée : les alertes restent silencieuses et la disponibilité n’est pas décomptée.',
			start:
				'Les alertes sont supprimées et la disponibilité n’est pas décomptée. Les vérifications continuent, vous pouvez donc voir le service revenir.',
			start_hint: 'Vous pouvez toujours y mettre fin plus tôt. L’absence d’option sans limite est volontaire.',
			remove:
				'Supprimer « {{title}} » supprime aussi ses occurrences planifiées. Les alertes reprennent immédiatement.',
		},
		actions: {
			schedule: 'Planifier une fenêtre',
			start: 'Maintenance',
			end: 'Terminer maintenant',
		},
		badge: {
			in_progress: 'Maintenance, {{duration}} restantes',
			scheduled: 'Maintenance dans {{duration}}',
		},
		recurrence: {
			ONCE: 'Ponctuelle',
			DAILY: 'Quotidienne',
			WEEKLY: 'Hebdomadaire',
			MONTHLY: 'Mensuelle',
		},
		table: {
			title: 'Fenêtre',
			state: 'Prochaine occurrence',
			recurrence: 'Répétition',
			duration: 'Durée',
			monitors: 'Sondes',
			actions: 'Actions',
			no_occurrence: 'Rien de planifié',
			inactive: 'Désactivée',
		},
		form: {
			title_placeholder: 'Migration de base de données',
			probes: 'Sondes couvertes',
			probes_description: 'Une fenêtre sans sonde ne fait taire personne.',
			starts_at: 'Début',
			duration: 'Durée (minutes)',
			duration_description: 'Une fenêtre que personne ne referme est une supervision silencieusement désarmée.',
			recurrence: 'Répétition',
			timezone: 'Fuseau horaire',
			timezone_description: 'Maintient une fenêtre de 02:00 à 02:00 des deux côtés d’un changement d’heure.',
			recurrence_until: 'Répéter jusqu’au',
			recurrence_until_description: 'Laissez vide pour répéter indéfiniment.',
			active: 'Activée',
			active_description: 'Désactivez pour conserver la fenêtre sans qu’elle ne supprime quoi que ce soit.',
			is_public: 'Afficher sur la page de statut',
			is_public_description:
				'Publier le calendrier d’un parc interne, c’est aussi publier les moments où ses défenses sont baissées.',
		},
		alerts: {
			created: 'Fenêtre de maintenance « {{title}} » planifiée',
			updated: 'Fenêtre de maintenance « {{title}} » mise à jour',
			removed: 'Fenêtre de maintenance supprimée',
			started: 'Maintenance démarrée',
			ended: 'Maintenance terminée',
			occurrence_cancelled: 'Occurrence annulée',
		},
		empty: {
			title: 'Aucune fenêtre de maintenance pour l’instant',
			description: 'Planifiez-en une avant votre prochain déploiement, et les alertes resteront silencieuses.',
		},
	},

	profile: {
		title: {
			update_profile: 'Modifier le profil',
			update_password: 'Modifier le mot de passe',
			create_first_user: 'Créez votre compte',
			preferences: 'Préférences d’affichage',
		},
		tabs: {
			account: 'Compte',
			password: 'Mot de passe',
			notifications: 'Notifications',
			preferences: 'Préférences',
		},
		description: {
			current_password: 'Par sécurité, confirmez le mot de passe que vous utilisez aujourd’hui.',
			update_password:
				'Changer votre mot de passe vous déconnectera de toutes vos sessions. Vous devrez vous reconnecter.',
			update_profile: 'Gérez vos informations personnelles pour garder votre compte exact et sûr.',
			create_first_user:
				'Saisissez votre adresse e-mail pour créer le premier utilisateur et initialiser l’application.',
			preferences:
				'Enregistrées dans ce navigateur, pas sur votre compte : un autre navigateur garde son propre choix.',
			language: 'Prise en compte immédiatement. Les dates, heures et nombres suivent le même choix.',
			theme: '« Système » suit le réglage de votre système d’exploitation.',
		},
		label: {
			current_password: 'Mot de passe actuel',
			password: 'Nouveau mot de passe',
			password_confirm: 'Confirmation du mot de passe',
			language: 'Langue',
			theme: 'Thème',
		},
		alerts: {
			update_profile: 'Profil mis à jour',
			update_password: 'Mot de passe mis à jour',
			create_first_user: 'Premier utilisateur créé',
		},
	},

	dashboard: {
		title: {
			monitors: 'Sondes',
			monitors_up: 'Sondes UP',
			monitors_down: 'Sondes DOWN',
			uptime: 'Disponibilité moyenne',
			notifications: 'Notifications',
			last_24_hours: 'Dernières 24 heures',
			response_time_average: 'Temps de réponse moyen',
			incidents: 'Incidents',
			checks_executed: 'Vérifications exécutées',
		},

		description: {
			monitors: 'Total des sondes',
			monitors_up: 'Fonctionnelles',
			monitors_down: 'Hors service',
			uptime: 'Tous services confondus',
			avg_response_time: 'Latence moyenne',
			on_24_hours: 'Sur 24 heures',
			latency_average: 'Latence moyenne',
			executing: 'Exécutées',
			currently_incidents: 'Incidents actuellement en cours',
		},

		events: {
			title: 'Événements récents',
			description: 'Changements d’état de vos sondes',
			empty: 'Aucun changement d’état sur les 7 derniers jours',
		},

		legend: {
			up: 'En service',
			down: 'Hors service',
		},

		table: {
			services: 'Services',
			times: 'Temps',
			status: 'Statut',
			empty: 'Aucune donnée dans le tableau',
		},
	},

	timeRanger: {
		last_1_hour: 'Dernière heure',
		last_3_hours: 'Dernières 3 heures',
		last_6_hours: 'Dernières 6 heures',
		last_24_hours: 'Dernières 24 heures',
		last_7_days: 'Derniers 7 jours',
	},

	layout: {
		breadcrumb: {
			monitor: 'Sonde',
		},
		theme: {
			switch_to_light: 'Passer au thème clair',
			switch_to_dark: 'Passer au thème sombre',
			system: 'Système',
			light: 'Clair',
			dark: 'Sombre',
		},
		language: {
			label: 'Langue',
			en: 'English',
			fr: 'Français',
			de: 'Deutsch',
			es: 'Español',
		},
		updated: {
			seconds_one: 'Mis à jour il y a {{count}} s',
			seconds_other: 'Mis à jour il y a {{count}} s',
			minutes_one: 'Mis à jour il y a {{count}} min',
			minutes_other: 'Mis à jour il y a {{count}} min',
		},
		sidebar: {
			monitors: 'Sondes',
			search: {
				placeholder: 'Filtrer les sondes',
				label: 'Filtrer les sondes par nom ou description',
				clear: 'Effacer le filtre',
				count: '{{visible}} sur {{total}}',
				empty: 'Aucune sonde ne correspond à « {{query}} ».',
			},
			settings: 'Paramètres',
			logout: 'Se déconnecter',
			dashboard: 'Tableau de bord',
			status_page: 'Page de statut',
			maintenances: 'Maintenance',
			new_monitor: 'Nouvelle sonde',
		},
	},
};

export default fr;
