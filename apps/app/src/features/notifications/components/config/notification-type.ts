import NotificationTypeEnum from '@/features/notifications/enums/notification-type-enum.ts';
import type { TFunction } from 'i18next';

export function buildNotificationFieldsConfig(t: TFunction) {
	const chatWebhookFields = (placeholder: string) => [
		{
			name: 'webhook_url',
			label: t('form.label.webhook_url'),
			input_type: 'text',
			placeholder,
		},
		{
			name: 'username',
			label: t('form.label.bot_name'),
			input_type: 'text',
			placeholder: t('form.placeholder.bot_name'),
		},
	];

	return {
		[NotificationTypeEnum.DISCORD]: chatWebhookFields('https://discord.com/api/webhooks/...'),
		[NotificationTypeEnum.TEAMS]: chatWebhookFields('https://microsoft-teams.com/api/webhooks/...'),
		[NotificationTypeEnum.SLACK]: chatWebhookFields('https://hooks.slack.com/services/...'),
		[NotificationTypeEnum.WEBHOOK]: [
			{
				name: 'url',
				label: t('form.label.url'),
				input_type: 'text',
				placeholder: 'https://example.com/webhook',
			},
			{
				name: 'method',
				label: t('form.label.method'),
				input_type: 'select',
				options: ['POST', 'GET'],
			},
		],
		[NotificationTypeEnum.TELEGRAM]: [
			{
				name: 'bot_token',
				label: t('form.label.bot_token'),
				input_type: 'password',
				placeholder: '123456789:AAH...',
				description: t('form.description.bot_token'),
			},
			{
				name: 'chat_id',
				label: t('form.label.chat_id'),
				input_type: 'text',
				placeholder: '-1001234567890',
				description: t('form.description.chat_id'),
			},
			{
				name: 'message_thread_id',
				label: t('form.label.message_thread_id'),
				input_type: 'number',
				description: t('form.description.message_thread_id'),
			},
		],
		[NotificationTypeEnum.NTFY]: [
			{
				name: 'server_url',
				label: t('form.label.server_url'),
				input_type: 'text',
				placeholder: 'https://ntfy.sh',
				description: t('form.description.server_url'),
			},
			{
				name: 'topic',
				label: t('form.label.topic'),
				input_type: 'text',
				placeholder: 'uptime-kotlin-alerts',
				description: t('form.description.topic'),
			},
			{
				name: 'access_token',
				label: t('form.label.access_token'),
				input_type: 'password',
				placeholder: 'tk_...',
				description: t('form.description.access_token'),
			},
			{
				name: 'remove_access_token',
				label: t('form.label.remove_access_token'),
				input_type: 'switch',
				update_only: true,
			},
		],
		[NotificationTypeEnum.GOTIFY]: [
			{
				name: 'server_url',
				label: t('form.label.server_url'),
				input_type: 'text',
				placeholder: 'https://gotify.example.com',
				description: t('form.description.gotify_server_url'),
			},
			{
				name: 'app_token',
				label: t('form.label.app_token'),
				input_type: 'password',
				placeholder: 'AbCdEf...',
				description: t('form.description.app_token'),
			},
		],
		[NotificationTypeEnum.MAIL]: [
			{
				name: 'hostname',
				label: t('form.label.hostname'),
				input_type: 'text',
				placeholder: t('form.placeholder.mailer_url'),
			},
			{
				name: 'port',
				label: t('form.label.port'),
				input_type: 'number',
				placeholder: '587',
			},
			{
				name: 'username',
				label: t('form.label.username'),
				input_type: 'text',
				placeholder: 'uptime-kotlin@exemple.com',
			},
			{
				name: 'password',
				label: t('form.label.password'),
				input_type: 'password',
				placeholder: '********',
			},
			{
				name: 'starttls',
				label: t('form.label.starttls'),
				input_type: 'switch',
			},
			{
				name: 'from',
				label: t('form.label.address_from'),
				input_type: 'email',
				placeholder: 'uptime-kotlin@exemple.com',
			},
			{
				name: 'to',
				label: t('form.label.address_to'),
				input_type: 'email',
				placeholder: 'uptime-kotlin@exemple.com, uptime-kotlin2@exemple.com',
			},
		],
	};
}

export type NotificationFieldsConfig = ReturnType<typeof buildNotificationFieldsConfig>;
