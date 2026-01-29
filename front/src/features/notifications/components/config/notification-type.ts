import NotificationTypeEnum from '@/features/notifications/enums/notification-type-enum.ts';
import i18n from '@/lang/i18n.ts';

const NOTIFICATION_FIELDS_CONFIG = {
	[NotificationTypeEnum.DISCORD]: [
		{
			name: 'url_webhook',
			label: i18n.t('form.label.webhook_url'),
			input_type: 'text',
			placeholder: 'https://discord.com/api/webhooks/...',
		},
		{
			name: 'name_reboot',
			label: 'Name reboot',
			input_type: 'text',
			placeholder: 'Uptime kotlin',
		},
	],
	[NotificationTypeEnum.TEAMS]: [
		{
			name: 'url_webhook',
			label: i18n.t('form.label.webhook_url'),
			input_type: 'text',
			placeholder: 'https://microsoft-teams.com/api/webhooks/...',
		},
		{
			name: 'name_reboot',
			label: 'Name reboot',
			input_type: 'text',
			placeholder: 'Uptime kotlin',
		},
	],
	[NotificationTypeEnum.MAIL]: [
		{
			name: 'hostname',
			label: i18n.t('form.label.hostname'),
			input_type: 'text',
			placeholder: i18n.t('form.placeholder.mailer_url'),
		},
		{
			name: 'port',
			label: i18n.t('form.label.port'),
			input_type: 'number',
			placeholder: '587',
		},
		{
			name: 'username',
			label: i18n.t('form.label.username'),
			input_type: 'text',
			placeholder: 'uptime-kotlin@exemple.com',
		},
		{
			name: 'password',
			label: i18n.t('form.label.password'),
			input_type: 'password',
			placeholder: '********',
		},
		{
			name: 'starttls',
			label: i18n.t('form.label.starttls'),
			input_type: 'switch',
		},
		{
			name: 'mail_from',
			label: i18n.t('form.label.address_from'),
			input_type: 'email',
			placeholder: 'uptime-kotlin@exemple.com',
		},
		{
			name: 'mail_to',
			label: i18n.t('form.label.address_to'),
			input_type: 'email',
			placeholder: 'uptime-kotlin@exemple.com, uptime-kotlin2@exemple.com',
		},
	],
};

export default NOTIFICATION_FIELDS_CONFIG;
