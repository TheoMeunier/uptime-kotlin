enum NotificationTypeEnum {
	DISCORD = 'DISCORD',
	MAIL = 'MAIL',
	TEAMS = 'TEAMS',
	SLACK = 'SLACK',
	WEBHOOK = 'WEBHOOK',
	TELEGRAM = 'TELEGRAM',
	NTFY = 'NTFY',
	GOTIFY = 'GOTIFY',
}

export const NOTIFICATION_TYPE_LABELS: Record<NotificationTypeEnum, string> = {
	[NotificationTypeEnum.DISCORD]: 'Discord',
	[NotificationTypeEnum.MAIL]: 'Email',
	[NotificationTypeEnum.TEAMS]: 'Microsoft Teams',
	[NotificationTypeEnum.SLACK]: 'Slack',
	[NotificationTypeEnum.WEBHOOK]: 'Webhook',
	[NotificationTypeEnum.TELEGRAM]: 'Telegram',
	[NotificationTypeEnum.NTFY]: 'ntfy',
	[NotificationTypeEnum.GOTIFY]: 'Gotify',
};

export default NotificationTypeEnum;
