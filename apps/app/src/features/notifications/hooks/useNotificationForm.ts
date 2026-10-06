import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import i18n from '@/lang/i18n.ts';

const baseStoreNotificationSchema = z.object({
	name: z.string().min(3).max(255),
	is_default: z.boolean().optional(),
});

function chatWebhookSchema<T extends 'DISCORD' | 'TEAMS' | 'SLACK'>(type: T) {
	return baseStoreNotificationSchema.extend({
		notification_type: z.literal(type),
		webhook_url: z.url(),
		username: z.string().min(3).max(255),
	});
}

const webhookNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('WEBHOOK'),
	url: z.url(),
	method: z.enum(['POST', 'GET']),
});

const telegramNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('TELEGRAM'),
	// Empty on update keeps the stored token, which the API never sends back.
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

const MailNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('MAIL'),
	hostname: z.url(),
	port: z.number().min(1).max(65535),
	username: z.email().min(3).max(255),
	password: z.string().min(3).max(255).nullable(),
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
]);

export type NotificationFormMode = 'create' | 'update';
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

		if (mode === 'create' && data.notification_type === 'MAIL' && !data.password) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				message: i18n.t('validation.password_required'),
				path: ['password'],
			});
		}
	});
}
