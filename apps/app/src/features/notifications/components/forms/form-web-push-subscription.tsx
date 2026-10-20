import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import type { UseFormReturn } from 'react-hook-form';
import { BellPlus, TriangleAlert, X } from 'lucide-react';
import { Field, FieldDescription, FieldError, FieldLabel } from '@/components/atoms/field.tsx';
import { Button } from '@/components/atoms/button.tsx';
import { Input } from '@/components/atoms/input.tsx';
import { Badge } from '@/components/atoms/badge.tsx';
import notificationService from '@/features/notifications/services/notification-service.ts';
import type { FieldConfig } from '@/features/notifications/types/notification.type.ts';
import type {
	StoreNotificationSchema,
	WebPushSubscriptionValue,
} from '@/features/notifications/hooks/useNotificationForm.ts';
import {
	describeThisBrowser,
	getCurrentSubscription,
	getNotificationPermission,
	getWebPushSupport,
	subscribeThisBrowser,
	WebPushPermissionError,
	type WebPushSubscriptionKeys,
} from '@/lib/web-push.ts';

interface FormWebPushSubscriptionProps {
	field: FieldConfig;
	form: UseFormReturn<StoreNotificationSchema>;
}

function pushServiceHost(endpoint: string) {
	try {
		return new URL(endpoint).host;
	} catch {
		return endpoint;
	}
}

export default function FormWebPushSubscription({ field, form }: FormWebPushSubscriptionProps) {
	const { t } = useTranslation();
	const support = getWebPushSupport();
	const [permission, setPermission] = useState(getNotificationPermission);
	const [browserEndpoint, setBrowserEndpoint] = useState<string | null>(null);
	const [isSubscribing, setIsSubscribing] = useState(false);
	const [failure, setFailure] = useState<string | null>(null);

	const { data: publicKey, isError } = useQuery({
		queryKey: ['web-push-public-key'],
		queryFn: () => notificationService.getWebPushPublicKey(),
		enabled: support === 'supported',
		staleTime: Infinity,
	});

	const subscriptions: WebPushSubscriptionValue[] =
		(form.watch('subscriptions' as never) as unknown as WebPushSubscriptionValue[] | undefined) ?? [];

	const setSubscriptions = (next: WebPushSubscriptionValue[]) =>
		form.setValue('subscriptions' as never, next as never, { shouldDirty: true, shouldValidate: true });

	const currentSubscriptions = () =>
		(form.getValues('subscriptions' as never) as unknown as WebPushSubscriptionValue[] | undefined) ?? [];

	const upsert = (keys: WebPushSubscriptionKeys, addIfMissing: boolean) => {
		const list = currentSubscriptions();
		const index = list.findIndex((subscription) => subscription.endpoint === keys.endpoint);
		if (index >= 0) {
			setSubscriptions(list.map((subscription, i) => (i === index ? { ...subscription, ...keys } : subscription)));
		} else if (addIfMissing) {
			setSubscriptions([...list, { ...keys, label: describeThisBrowser() }]);
		}
		setBrowserEndpoint(keys.endpoint);
	};

	useEffect(() => {
		if (!publicKey) return;
		let cancelled = false;
		getCurrentSubscription(publicKey)
			.then((current) => {
				if (!cancelled && current) upsert(current, false);
			})
			.catch(() => undefined);
		return () => {
			cancelled = true;
		};
	}, [publicKey]);

	const addThisBrowser = async () => {
		if (!publicKey) return;
		setIsSubscribing(true);
		setFailure(null);
		try {
			upsert(await subscribeThisBrowser(publicKey), true);
		} catch (error) {
			if (!(error instanceof WebPushPermissionError)) setFailure(t('form.web_push.failed'));
		} finally {
			setPermission(getNotificationPermission());
			setIsSubscribing(false);
		}
	};

	const thisBrowserListed = subscriptions.some((subscription) => subscription.endpoint === browserEndpoint);
	const unavailable =
		support === 'insecure'
			? t('form.web_push.insecure')
			: support === 'unsupported'
				? t('form.web_push.unsupported')
				: permission === 'denied'
					? t('form.web_push.denied')
					: isError
						? t('form.web_push.failed')
						: null;
	const error = (form.formState.errors as Record<string, { message?: string }>).subscriptions?.message;

	return (
		<Field>
			<FieldLabel className="block text-sm font-medium text-foreground">{field.label}</FieldLabel>
			{field.description && <FieldDescription>{field.description}</FieldDescription>}

			{subscriptions.length === 0 ? (
				<p className="text-sm text-muted-foreground">{t('form.web_push.empty')}</p>
			) : (
				<ul className="divide-y rounded-md border">
					{subscriptions.map((subscription, index) => (
						<li key={subscription.endpoint} className="flex items-center gap-2 p-2">
							<div className="min-w-0 flex-1">
								<Input
									value={subscription.label ?? ''}
									placeholder={t('form.web_push.label_placeholder')}
									aria-label={t('form.web_push.label_placeholder')}
									maxLength={100}
									className="h-8"
									onChange={(event) =>
										setSubscriptions(
											currentSubscriptions().map((item, i) =>
												i === index ? { ...item, label: event.target.value } : item
											)
										)
									}
								/>
								<p className="mt-1 truncate text-xs text-muted-foreground">{pushServiceHost(subscription.endpoint)}</p>
							</div>
							{subscription.endpoint === browserEndpoint && (
								<Badge variant="secondary">{t('form.web_push.this_browser')}</Badge>
							)}
							<Button
								type="button"
								variant="ghost"
								size="icon"
								aria-label={t('form.web_push.remove')}
								title={t('form.web_push.remove')}
								onClick={() => setSubscriptions(currentSubscriptions().filter((_, i) => i !== index))}
							>
								<X aria-hidden />
							</Button>
						</li>
					))}
				</ul>
			)}

			{unavailable ? (
				<p className="flex items-start gap-2 text-sm text-amber-700 dark:text-amber-400">
					<TriangleAlert className="mt-0.5 size-4 shrink-0" aria-hidden />
					{unavailable}
				</p>
			) : (
				!thisBrowserListed && (
					<Button
						type="button"
						variant="outline"
						className="w-fit"
						onClick={addThisBrowser}
						disabled={!publicKey || isSubscribing}
					>
						<BellPlus aria-hidden />
						{isSubscribing ? t('form.web_push.subscribing') : t('form.web_push.subscribe')}
					</Button>
				)
			)}

			{failure && <p className="text-sm text-destructive">{failure}</p>}
			<FieldError>{error}</FieldError>
			<p className="text-xs text-muted-foreground">{t('form.web_push.os_hint')}</p>
		</Field>
	);
}
