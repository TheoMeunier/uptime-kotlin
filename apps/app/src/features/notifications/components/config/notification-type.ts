import NotificationTypeEnum from '@/features/notifications/enums/notification-type-enum.ts';
import type { TFunction } from 'i18next';

// Fabrique, et non constante : voir probe-type.ts.
export function buildNotificationFieldsConfig(t: TFunction) {
	return {
		[NotificationTypeEnum.DISCORD]: [
			{
				name: 'webhook_url',
				label: t('form.label.webhook_url'),
				input_type: 'text',
				placeholder: 'https://discord.com/api/webhooks/...',
			},
			{
				name: 'username',
				label: t('form.label.bot_name'),
				input_type: 'text',
				placeholder: t('form.placeholder.bot_name'),
			},
		],
		[NotificationTypeEnum.TEAMS]: [
			{
				name: 'webhook_url',
				label: t('form.label.webhook_url'),
				input_type: 'text',
				placeholder: 'https://microsoft-teams.com/api/webhooks/...',
			},
			{
				name: 'username',
				label: t('form.label.bot_name'),
				input_type: 'text',
				placeholder: t('form.placeholder.bot_name'),
			},
		],
		[NotificationTypeEnum.SLACK]: [
			{
				name: 'webhook_url',
				label: t('form.label.webhook_url'),
				input_type: 'text',
				placeholder: 'https://hooks.slack.com/services/...',
			},
			{
				name: 'username',
				label: t('form.label.bot_name'),
				input_type: 'text',
				placeholder: t('form.placeholder.bot_name'),
			},
		],
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
