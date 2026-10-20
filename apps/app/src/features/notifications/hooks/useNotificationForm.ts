import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import i18n from '@/lang/i18n.ts';
import { isValidHostnameOrIp } from '@/lib/hostname.ts';

const baseStoreNotificationSchema = z.object({
	name: z.string().min(3).max(255),
	is_default: z.boolean().optional(),
});

function chatWebhookSchema<T extends 'DISCORD' | 'TEAMS' | 'SLACK'>(type: T) {
	return baseStoreNotificationSchema.extend({
		notification_type: z.literal(type),
		webhook_url: z.url(),
		username: z.string().trim().max(255).nullish(),
	});
}

const webhookNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('WEBHOOK'),
	url: z.url(),
	method: z.enum(['POST', 'GET']),
});

const telegramNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('TELEGRAM'),
	bot_token: z
		.string()
		.trim()
		.regex(/^(\d+:[A-Za-z0-9_-]+)?$/, i18n.t('validation.telegram_bot_token'))
		.nullable()
		.optional(),
	chat_id: z
		.string()
		.trim()
		.regex(/^(-?\d+|@[A-Za-z][A-Za-z0-9_]{3,})$/, i18n.t('validation.telegram_chat_id')),
	message_thread_id: z.number().int().min(1).optional().nullable(),
});

const ntfyNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('NTFY'),
	server_url: z.url(),
	topic: z
		.string()
		.trim()
		.regex(/^[-_A-Za-z0-9]{1,64}$/, i18n.t('validation.ntfy_topic')),
	access_token: z.string().trim().optional().nullable(),
	remove_access_token: z.boolean().optional(),
});

const gotifyNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('GOTIFY'),
	server_url: z.url(),
	app_token: z.string().trim().optional().nullable(),
});

const webPushSubscriptionSchema = z.object({
	endpoint: z.string(),
	p256dh: z.string().optional().nullable(),
	auth: z.string().optional().nullable(),
	label: z.string().trim().max(100).optional().nullable(),
});

export type WebPushSubscriptionValue = z.infer<typeof webPushSubscriptionSchema>;

const webPushNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('WEB_PUSH'),
	subscriptions: z.array(webPushSubscriptionSchema).optional(),
});

const MailNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('MAIL'),
	hostname: z.string().trim().refine(isValidHostnameOrIp, i18n.t('validation.hostname')),
	port: z.number().min(1).max(65535),
	username: z.email().min(3).max(255),
	password: z.string().max(255).nullish(),
	starttls: z.boolean().optional(),
	to: z.email().min(3).max(255),
	from: z.email().min(3).max(255),
});

export const storeNotificationSchema = z.discriminatedUnion('notification_type', [
	chatWebhookSchema('DISCORD'),
	MailNotificationSchema,
	chatWebhookSchema('TEAMS'),
	chatWebhookSchema('SLACK'),
	webhookNotificationSchema,
	telegramNotificationSchema,
	ntfyNotificationSchema,
	gotifyNotificationSchema,
	webPushNotificationSchema,
]);

export type NotificationFormMode = 'create' | 'update';

export function withEmptyFieldsAsNull<T extends Record<string, unknown>>(values: T): T {
	const cleaned: Record<string, unknown> = { ...values };
	for (const field of ['password', 'username']) {
		if (cleaned.notification_type !== 'MAIL' || field === 'password') {
			if (cleaned[field] === '') cleaned[field] = null;
		}
	}
	return cleaned as T;
}
export type StoreNotificationSchema = z.infer<typeof storeNotificationSchema>;

export default function useNotificationForm(
	{ defaultValues, mode }: { defaultValues: Partial<StoreNotificationSchema>; mode: NotificationFormMode } = {
		defaultValues: {},
		mode: 'create',
	}
) {
	const form = useForm<StoreNotificationSchema>({
		resolver: zodResolver(createNotificationSchema(mode)),
		defaultValues: {
			...defaultValues,
		},
	});

	return {
		form,
		errors: form.formState.errors,
	};
}

function createNotificationSchema(mode: NotificationFormMode) {
	return storeNotificationSchema.superRefine((data, ctx) => {
		if (mode === 'create' && data.notification_type === 'TELEGRAM' && !data.bot_token) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				message: i18n.t('validation.telegram_bot_token'),
				path: ['bot_token'],
			});
		}

		if (mode === 'create' && data.notification_type === 'GOTIFY' && !data.app_token) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				message: i18n.t('validation.gotify_app_token'),
				path: ['app_token'],
			});
		}

		if (data.notification_type === 'WEB_PUSH' && !data.subscriptions?.length) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				message: i18n.t('validation.web_push_subscription'),
				path: ['subscriptions'],
			});
		}

		if (mode === 'create' && data.notification_type === 'MAIL' && !data.password) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				message: i18n.t('validation.password_required'),
				path: ['password'],
			});
		}
	});
}
