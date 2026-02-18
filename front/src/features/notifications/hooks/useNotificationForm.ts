import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const baseStoreNotificationSchema = z.object({
	name: z.string().min(3).max(255),
	is_default: z.boolean().optional(),
});

const discordNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('DISCORD'),
	webhook_url: z.url(),
	username: z.string().min(3).max(255),
});

const teamsNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('TEAMS'),
	webhook_url: z.url(),
	username: z.string().min(3).max(255),
});

const slackNotificationSchema = baseStoreNotificationSchema.extend({
	notification_type: z.literal('SLACK'),
	webhook_url: z.url(),
	username: z.string().min(3).max(255),
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
	discordNotificationSchema,
	MailNotificationSchema,
	teamsNotificationSchema,
	slackNotificationSchema,
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
		if (mode === 'create' && data.notification_type === 'MAIL' && !data.password) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				message: 'Password is required',
				path: ['password'],
			});
		}
	});
}
