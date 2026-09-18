import type en from '@/lang/en.ts';

// Tipado sobre `en`: una clave que falte o que sobre es un error de compilación.
const es: typeof en = {
	errors: {
		load: {
			title: 'No se han podido cargar estos datos',
			description: 'La API no ha respondido. Puede que se esté reiniciando o que no sea accesible desde su navegador.',
		},
	},

	unsaved_changes: {
		title: '¿Salir sin guardar?',
		description: 'Los cambios de este formulario no se han guardado. Se perderán si sales de esta página.',
		stay: 'Seguir editando',
		leave: 'Salir sin guardar',
	},

	app: {
		loading: 'Cargando Uptime Kotlin',
		loading_short: 'Cargando…',
	},

	status: {
		healthy: 'Operativo',
		degraded: 'Degradado',
		unhealthy: 'Caído',
		paused: 'En pausa',
	},

	form: {
		label: {
			full_name: 'Nombre completo',
			email: 'Correo electrónico',
			password: 'Contraseña',
			confirmation_password: 'Confirmar contraseña',
			starttls: 'StartTLS',
			webhook_url: 'URL del webhook',
			name: 'Nombre {{ entity }}',
			description: 'Descripción',
			hostname: 'Nombre de host',
			port: 'Puerto',
			username: 'Nombre de usuario',
			address_from: 'Remitente',
			address_to: 'Destinatario',
			host: 'Host',
			url: 'URL',
			enabled: 'Activado',
			method: 'Método',
			bot_name: 'Nombre del bot',
		},
		placeholder: {
			email: 'juan.perez@ejemplo.com',
			password: '********',
			mailer_url: 'smtp.ejemplo.com',
			full_name: 'Juan Pérez',
			admin_email: 'admin@uptime-kotlin.com',
			bot_name: 'Uptime Kotlin',
		},
		description: {
			password: 'Al menos 8 caracteres',
		},
	},

	button: {
		login: 'Iniciar sesión',
		retry: 'Reintentar',
		close: 'Cerrar',
		cancel: 'Cancelar',
		loading: 'Iniciando sesión…',
		exporting: 'Exportando…',
		create: 'Crear {{entity}}',
		update: 'Editar {{entity}}',
		remove: 'Eliminar {{entity}}',
		purge: 'Purgar',
		removing: 'Eliminando…',
		purging: 'Purgando…',
		saving: 'Guardando…',
		save: 'Guardar {{entity}}',
		test: 'Probar {{entity}}',

		actions: {
			pause: 'Pausar',
			resume: 'Reanudar',
			remove: 'Eliminar',
			edit: 'Editar',
			export_csv: 'Exportar CSV',
			more: 'Más acciones',
			check_now: 'Comprobar ahora',
			purge_logs: 'Purgar registros',
			duplicate: 'Duplicar',
		},
	},

	entity: {
		monitor: 'monitor',
		notification: 'notificación',
		profile: 'perfil',
		maintenance: 'ventana de mantenimiento',
		first_account: 'primera cuenta',
	},

	validation: {
		passwords_mismatch: 'Las contraseñas no coinciden',
		password_must_differ: 'La nueva contraseña debe ser distinta de la actual',
		password_required: 'La contraseña es obligatoria',
		alert_repeat_seconds: 'Use 0 para desactivar, o al menos 60 segundos',
		recurrence_until_after_start: 'La recurrencia debe terminar después de su primera repetición',
	},

	select: {
		placeholder: 'Seleccionar',
		empty: 'Sin resultados.',
	},

	pages: {
		status_page: {
			title: 'Panel de estado',
			subtitle: 'Supervisión de la infraestructura en tiempo real',
			description: {
				last_update: 'Última actualización: ',
				automatic_refresh: 'Actualización automática',
			},
			uptime_30d: 'Disponibilidad a 30 días',
			down_for: 'Caído desde hace {{duration}}',
			planned_downtime: '{{duration}} planificadas',
			verdict: {
				operational_one: 'Todos los sistemas operativos',
				operational_other: 'Los {{count}} servicios están operativos',
				degraded_one: '1 de {{total}} servicios está degradado',
				degraded_other: '{{count}} de {{total}} servicios están degradados',
				down_one: '1 de {{total}} servicios tiene una incidencia',
				down_other: '{{count}} de {{total}} servicios tienen una incidencia',
				maintenance_one: '1 servicio en mantenimiento',
				maintenance_other: '{{count}} servicios en mantenimiento',
			},
			empty: {
				title: 'Todavía no hay ningún monitor',
				description: 'Añada un monitor para empezar a vigilar su infraestructura.',
			},
		},
		login: {
			title: 'Acceda a su cuenta',
			description: 'Introduzca sus credenciales para iniciar sesión.',
		},
	},

	notifications: {
		title: {
			create: 'Crear notificación',
			update: 'Editar notificación',
			remove: 'Eliminar notificación',
			notifications: 'Notificaciones',
		},
		description: {
			settings: 'Gestione sus notificaciones',
			remove: 'Esta acción es irreversible. Todos los datos asociados se eliminarán definitivamente',
		},
		empty: {
			title: 'Todavía no hay ningún canal de notificación',
			description: 'Cree uno para recibir avisos cuando un monitor caiga.',
		},
		label: {
			is_default: 'Predeterminada',
			set_as_default: 'Usar esta notificación de forma predeterminada',
			type_notification: 'Tipo de notificación',
			notification_name: 'Nombre de la notificación',
		},
		placeholder: {
			notification_name: 'Bot de Discord',
		},
		alerts: {
			create: 'Notificación {{ data }} creada',
			update: 'Notificación {{ data }} actualizada',
			remove: 'Notificación eliminada',
			testing: {
				success: 'Notificación probada correctamente',
				error: 'Error al probar la notificación',
			},
		},
	},

	monitors: {
		protocol_group: {
			network: 'Web y red',
			database: 'Bases de datos',
			messaging: 'Mensajería y correo',
		},

		section: {
			target: 'Qué supervisar',
			schedule: 'Planificación',
			schedule_description:
				'Con qué frecuencia se ejecuta la comprobación y cómo reintenta antes de lanzar una alerta.',
			settings: 'Ajustes',
			notifications_description: 'Canales avisados cuando este monitor cae.',
		},

		duplicate: {
			copy_name: '{{name}} (copia)',
			description: 'Copia de {{name}}. Ajuste el destino antes de guardar: no se crea nada hasta entonces.',
		},

		title: {
			create: 'Crear monitor',
			update: 'Editar monitor',
			remove: 'Eliminar monitor',
			purge_logs: 'Purgar los registros del monitor',
			pause: '¿Pausar este monitor?',
			resume: '¿Reanudar este monitor?',
			final_hour: 'Última hora',
			http_request_assertions: 'Petición HTTP y aserciones',
			duplicate: 'Duplicar monitor',
		},
		label: {
			http_method: 'Método HTTP',
			follow_redirects: 'Seguir redirecciones',
			max_latency_ms: 'Latencia máxima (ms)',
			tls_expiry_warning_days: 'Umbral de aviso de caducidad TLS (días)',
			request_body: 'Cuerpo de la petición',
			request_headers_json: 'Cabeceras de la petición (objeto JSON)',
			authentication: 'Autenticación',
			assertions_json: 'Aserciones (array JSON)',
			scenario_steps_json: 'Pasos del escenario (array JSON, opcional)',
			tcp_host: 'Host TCP',
			tcp_port: 'Puerto TCP',
			dns_server: 'Servidor DNS',
			dns_port: 'Puerto DNS',
			notification_certificate: 'Aviso de caducidad del certificado',
			ignore_certificate_errors: 'Ignorar los errores TLS / SSL en sitios HTTPS',
			http_code_allowed: 'Códigos de estado aceptados',
			dns_record: 'Registro DNS',
			ping_heartbeat_interval: 'Intervalo de latido',
			ping_max_packet: 'Pérdida máxima de paquetes',
			ping_size: 'Tamaño de los paquetes',
			ping_delay: 'Intervalo entre pings (ms)',
			postgresql_connection_string: 'Cadena de conexión de PostgreSQL',
			postgresql_query: 'Consulta',
			sqlserver_connection_string: 'Cadena de conexión de Microsoft SQL Server',
			sqlserver_query: 'Consulta',
			mysql_connection_string: 'Cadena de conexión de MySQL/MariaDB',
			mysql_query: 'Consulta',
			redis_connection_string: 'Cadena de conexión de Redis',
			redis_command: 'Comando',
			smtp_hostname: 'Nombre de host / dirección IP',
			smtp_port: 'Puerto',
			smtp_security: 'Seguridad SMTP',
			kafka_brokers: 'Brokers de Kafka',
			kafka_topic: 'Nombre del topic de Kafka',
			kafka_message: 'Mensaje del productor de Kafka',
			kafka_ssl: 'Activar SSL en Kafka',
			kafka_auto_topic_creation: 'Activar la creación automática de topics en Kafka',
			rabbitmq_management_nodes: 'Nodos de gestión de RabbitMQ',
			rabbitmq_username: 'Nombre de usuario de RabbitMQ',
			rabbitmq_password: 'Contraseña de RabbitMQ',
			protocol: 'Protocolo del monitor',
			interval: 'Intervalo de comprobación (s)',
			name_monitor: 'Nombre del monitor',
			retry: 'Reintentos',
			interval_retry: 'Intervalo entre reintentos (s)',
			alert_repeat_seconds: 'Reenviar la alerta cada (s)',
			remove_confirmation: 'Confirme el nombre del monitor',
		},
		tls: {
			expired: 'Certificado caducado hace {{days}} d',
			expires_in: 'El certificado caduca en {{days}} d',
			expires_today: 'El certificado caduca hoy',
			valid_for: 'Certificado válido {{days}} d',
			expires_on: 'Caduca el {{date}}',
			checked_at: 'Última lectura {{date}}',
		},
		uptime: {
			h24: 'Disponibilidad 24 h',
			d7: 'Disponibilidad 7 d',
			d30: 'Disponibilidad 30 d',
		},
		latency: {
			current: 'Actual',
			average: 'Media',
			max_peak: 'Pico máximo',
			min: 'Mínima',
		},
		chart: {
			title: 'Tiempo de respuesta',
			description: 'Tiempos de respuesta de las comprobaciones en {{range}}',
			response_time: 'Tiempo de respuesta (ms)',
			average_reference: 'media {{value}} ms',
			select_range: 'Elegir el periodo',
			loading: 'Cargando el gráfico…',
			empty: 'No hay datos en el periodo seleccionado',
		},
		logs: {
			title: 'Registros de supervisión',
			description: 'Actividad reciente del monitor',
			filter_all: 'Todos',
			filter_success: 'Correctos',
			filter_errors: 'Errores',
			no_message: 'Sin mensaje',
			empty_filter: 'No hay registros para este filtro',
			showing: '{{count}} de {{total}} mostrados — siga desplazándose',
		},
		description: {
			authentication_optional: 'Opcional. Elija Basic o Bearer solo si es necesario.',
			remove_confirmation: 'Escriba exactamente este valor para habilitar la eliminación:',
			max_latency_ms:
				'La comprobación falla si la respuesta es más lenta. Entre 1 y 5000 ms, ya que el cliente HTTP expira a los 5 s. Déjelo vacío para desactivar el umbral.',
			remove: 'Esta acción es irreversible. Todos los datos asociados se eliminarán definitivamente',
			check_now_disabled: 'Reanude este monitor para lanzar una comprobación.',
			purge_logs: 'Esto eliminará definitivamente todo el historial de este monitor.',
			dns_server: 'Cloudflare es el servidor predeterminado. Puede cambiar de resolutor en cualquier momento.',
			dns_port: 'Puerto del servidor DNS. 53 de forma predeterminada. Puede cambiarlo en cualquier momento.',
			smtp_security:
				'«SMTPS» prueba SMTP/TLS implícito; «Ignorar TLS» conecta en texto plano; «STARTTLS» conecta, envía STARTTLS y verifica el certificado del servidor. Ninguna de estas comprobaciones envía un correo.',
			rabbitmq_management_nodes:
				'Introduzca las URL de los nodos de gestión de RabbitMQ, con protocolo y puerto, separadas por comas. Ejemplo: https://node1.rabbitmq.com:15672',
			internal_retry: 'Número de reintentos antes de marcar el servicio como caído y enviar una notificación',
			alert_repeat_seconds:
				'Mientras el monitor siga caído, la alerta se reenvía por los mismos canales con este intervalo. 0 envía una sola notificación, como antes. Mínimo 60 s.',
			check_interval: 'Comprobación cada {{ interval }} segundos',
			enabled: 'Un monitor en pausa conserva su historial pero no ejecuta ninguna comprobación.',
			now: 'Ahora',
			one_hour_ago: 'Hace 1 hora',
			paused_slot: 'El monitor estaba en pausa, no se ejecutó ninguna comprobación.',
			pause:
				'Las comprobaciones se detienen hasta que lo reanude. El historial se conserva y no se enviará ninguna alerta.',
			resume: 'Las comprobaciones se reanudan de inmediato, con el intervalo configurado.',
		},
		option: {
			smtp_ignore_tls: 'Ignorar TLS',
			no_authentication: 'Sin autenticación',
			basic_authentication: 'Autenticación Basic',
			bearer_token: 'Token Bearer',
		},
		placeholder: {
			name_monitor: 'API de producción',
			username: 'Nombre de usuario',
			password: 'Contraseña',
			bearer_token: 'Token Bearer',
		},
		error: {
			invalid_json: 'JSON no válido',
		},
		alerts: {
			create: 'Monitor {{ data }} creado',
			update: 'Monitor {{ data }} actualizado',
			remove: 'Monitor eliminado',
			purge_logs: 'Registros del monitor purgados',
			check_now: 'Comprobación solicitada, el resultado aparecerá en unos segundos',
			check_now_already_running: 'Ya hay una comprobación en curso para este monitor',
			paused: 'Monitor en pausa',
			resumed: 'Monitor reanudado',
		},
	},

	maintenances: {
		title: {
			index: 'Ventanas de mantenimiento',
			start: 'Iniciar una ventana de mantenimiento',
			remove: '¿Eliminar esta ventana de mantenimiento?',
		},
		description: {
			index: 'Inactividad planificada: las alertas permanecen en silencio y no se descuenta disponibilidad.',
			start:
				'Las alertas se suprimen y no se descuenta disponibilidad. Las comprobaciones siguen ejecutándose, así que podrá ver el servicio volver.',
			start_hint: 'Siempre puede terminarla antes. La ausencia de una opción sin límite es deliberada.',
			remove:
				'Eliminar «{{title}}» elimina también sus repeticiones programadas. Las alertas se reanudan de inmediato.',
		},
		actions: {
			schedule: 'Programar una ventana',
			start: 'Mantenimiento',
			end: 'Terminar ahora',
		},
		badge: {
			in_progress: 'Mantenimiento, quedan {{duration}}',
			scheduled: 'Mantenimiento en {{duration}}',
		},
		recurrence: {
			ONCE: 'Puntual',
			DAILY: 'Diaria',
			WEEKLY: 'Semanal',
			MONTHLY: 'Mensual',
		},
		table: {
			title: 'Ventana',
			state: 'Próxima repetición',
			recurrence: 'Repetición',
			duration: 'Duración',
			monitors: 'Monitores',
			actions: 'Acciones',
			no_occurrence: 'Nada programado',
			inactive: 'Desactivada',
		},
		form: {
			title_placeholder: 'Migración de base de datos',
			probes: 'Monitores cubiertos',
			probes_description: 'Una ventana sin monitores no silencia nada.',
			starts_at: 'Inicio',
			duration: 'Duración (minutos)',
			duration_description: 'Una ventana que nadie cierra es una supervisión desarmada en silencio.',
			recurrence: 'Repetición',
			timezone: 'Zona horaria',
			timezone_description: 'Mantiene una ventana de las 02:00 a las 02:00 a ambos lados de un cambio de hora.',
			recurrence_until: 'Repetir hasta',
			recurrence_until_description: 'Déjelo vacío para repetir indefinidamente.',
			active: 'Activada',
			active_description: 'Desactívela para conservar la ventana sin que suprima nada.',
			is_public: 'Mostrar en la página de estado',
			is_public_description:
				'Publicar el calendario de un parque interno es publicar también cuándo están bajadas sus defensas.',
		},
		alerts: {
			created: 'Ventana de mantenimiento «{{title}}» programada',
			updated: 'Ventana de mantenimiento «{{title}}» actualizada',
			removed: 'Ventana de mantenimiento eliminada',
			started: 'Mantenimiento iniciado',
			ended: 'Mantenimiento terminado',
			occurrence_cancelled: 'Repetición cancelada',
		},
		empty: {
			title: 'Todavía no hay ninguna ventana de mantenimiento',
			description: 'Programe una antes de su próximo despliegue y las alertas permanecerán en silencio.',
		},
	},

	profile: {
		title: {
			update_profile: 'Editar perfil',
			update_password: 'Cambiar la contraseña',
			create_first_user: 'Cree su cuenta',
			preferences: 'Preferencias de visualización',
		},
		tabs: {
			account: 'Cuenta',
			password: 'Contraseña',
			notifications: 'Notificaciones',
			preferences: 'Preferencias',
		},
		description: {
			current_password: 'Por seguridad, confirme la contraseña que usa actualmente.',
			update_password: 'Cambiar la contraseña cerrará todas sus sesiones. Tendrá que volver a iniciar sesión.',
			update_profile: 'Gestione sus datos personales para mantener su cuenta exacta y segura.',
			create_first_user: 'Introduzca su correo electrónico para crear el primer usuario e inicializar la aplicación.',
			preferences: 'Se guardan en este navegador, no en su cuenta: otro navegador conserva su propia elección.',
			language: 'Se aplica de inmediato. Las fechas, horas y cifras siguen la misma elección.',
			theme: '«Sistema» sigue la configuración de su sistema operativo.',
		},
		label: {
			current_password: 'Contraseña actual',
			password: 'Nueva contraseña',
			password_confirm: 'Confirmar contraseña',
			language: 'Idioma',
			theme: 'Tema',
		},
		alerts: {
			update_profile: 'Perfil actualizado',
			update_password: 'Contraseña actualizada',
			create_first_user: 'Primer usuario creado',
		},
	},

	dashboard: {
		title: {
			monitors: 'Monitores',
			monitors_up: 'Monitores UP',
			monitors_down: 'Monitores DOWN',
			uptime: 'Disponibilidad media',
			notifications: 'Notificaciones',
			last_24_hours: 'Últimas 24 horas',
			response_time_average: 'Tiempo de respuesta medio',
			incidents: 'Incidencias',
			checks_executed: 'Comprobaciones ejecutadas',
		},

		description: {
			monitors: 'Total de monitores',
			monitors_up: 'Operativos',
			monitors_down: 'Caídos',
			uptime: 'Todos los servicios',
			avg_response_time: 'Latencia media',
			on_24_hours: 'En 24 horas',
			latency_average: 'Latencia media',
			executing: 'Ejecutadas',
			currently_incidents: 'Incidencias actualmente activas',
		},

		events: {
			title: 'Eventos recientes',
			description: 'Cambios de estado de sus monitores',
			empty: 'Ningún cambio de estado en los últimos 7 días',
		},

		legend: {
			up: 'Operativo',
			down: 'Caído',
		},

		table: {
			services: 'Servicios',
			times: 'Tiempos',
			status: 'Estado',
			empty: 'No hay datos en la tabla',
		},
	},

	timeRanger: {
		last_1_hour: 'Última hora',
		last_3_hours: 'Últimas 3 horas',
		last_6_hours: 'Últimas 6 horas',
		last_24_hours: 'Últimas 24 horas',
		last_7_days: 'Últimos 7 días',
	},

	layout: {
		breadcrumb: {
			monitor: 'Monitor',
		},
		theme: {
			switch_to_light: 'Cambiar al tema claro',
			switch_to_dark: 'Cambiar al tema oscuro',
			system: 'Sistema',
			light: 'Claro',
			dark: 'Oscuro',
		},
		language: {
			label: 'Idioma',
			en: 'English',
			fr: 'Français',
			de: 'Deutsch',
			es: 'Español',
		},
		updated: {
			seconds_one: 'Actualizado hace {{count}} s',
			seconds_other: 'Actualizado hace {{count}} s',
			minutes_one: 'Actualizado hace {{count}} min',
			minutes_other: 'Actualizado hace {{count}} min',
		},
		sidebar: {
			monitors: 'Monitores',
			search: {
				placeholder: 'Filtrar monitores',
				label: 'Filtrar monitores por nombre o descripción',
				clear: 'Borrar el filtro',
				count: '{{visible}} de {{total}}',
				empty: 'Ningún monitor coincide con «{{query}}».',
			},
			settings: 'Ajustes',
			logout: 'Cerrar sesión',
			dashboard: 'Panel',
			status_page: 'Página de estado',
			maintenances: 'Mantenimiento',
			new_monitor: 'Nuevo monitor',
		},
	},
};

export default es;
