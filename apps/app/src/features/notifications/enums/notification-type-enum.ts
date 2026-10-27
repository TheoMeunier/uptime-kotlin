enum NotificationTypeEnum {
	DISCORD = 'DISCORD',
	MAIL = 'MAIL',
	TEAMS = 'TEAMS',
	SLACK = 'SLACK',
	WEBHOOK = 'WEBHOOK',
	TELEGRAM = 'TELEGRAM',
	NTFY = 'NTFY',
	GOTIFY = 'GOTIFY',
	WEB_PUSH = 'WEB_PUSH',
	PAGERDUTY = 'PAGERDUTY',
	MATRIX = 'MATRIX',
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
	[NotificationTypeEnum.WEB_PUSH]: 'Web Push',
	[NotificationTypeEnum.PAGERDUTY]: 'PagerDuty',
	[NotificationTypeEnum.MATRIX]: 'Matrix',
};

export const NOTIFICATION_TYPE_GROUPS: { labelKey: string; types: NotificationTypeEnum[] }[] = [
	{
		labelKey: 'notifications.type_group.chat',
		types: [
			NotificationTypeEnum.DISCORD,
			NotificationTypeEnum.SLACK,
			NotificationTypeEnum.TEAMS,
			NotificationTypeEnum.TELEGRAM,
			NotificationTypeEnum.MATRIX,
		],
	},
	{
		labelKey: 'notifications.type_group.push',
		types: [NotificationTypeEnum.WEB_PUSH, NotificationTypeEnum.NTFY, NotificationTypeEnum.GOTIFY],
	},
	{
		labelKey: 'notifications.type_group.other',
		types: [NotificationTypeEnum.MAIL, NotificationTypeEnum.PAGERDUTY, NotificationTypeEnum.WEBHOOK],
	},
];

export default NotificationTypeEnum;
