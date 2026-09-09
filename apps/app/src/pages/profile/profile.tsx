import { useSearchParams } from 'react-router';
import { useTranslation } from 'react-i18next';
import { Bell, Lock, User } from 'lucide-react';
import { cn } from '@/lib/utils';
import UpdateProfileForm from '@/features/profile/components/forms/update-profile-form.tsx';
import UpdatePasswordProfileForm from '@/features/profile/components/forms/update-password-profile-form.tsx';
import ListingNotification from '@/features/notifications/components/listing-notification.tsx';

const SECTIONS = [
	{ id: 'account', labelKey: 'profile.tabs.account', icon: User },
	{ id: 'password', labelKey: 'profile.tabs.password', icon: Lock },
	{ id: 'notifications', labelKey: 'profile.tabs.notifications', icon: Bell },
] as const;

export default function Profile() {
	const { t } = useTranslation();

	/*
	 * The active section lives in the URL rather than in component state: a reload used to drop
	 * the user back on "account", and a link to the notification settings could not be shared.
	 */
	const [searchParams, setSearchParams] = useSearchParams();
	const requested = searchParams.get('tab');
	const active = SECTIONS.some((section) => section.id === requested) ? requested! : 'account';

	return (
		<div className="flex flex-col gap-8 md:flex-row md:gap-10">
			{/*
			 * No border and no background: with the app's own sidebar right next to it, a second
			 * bordered rail read as two nested navigations. Here the active item carries the
			 * emphasis instead. Scrolls horizontally on narrow screens rather than squeezing.
			 */}
			<aside className="md:w-52 md:shrink-0">
				<nav className="-mx-1 flex gap-1 overflow-x-auto px-1 pb-1 md:mx-0 md:flex-col md:overflow-visible md:px-0 md:pb-0">
					{SECTIONS.map(({ id, labelKey, icon: Icon }) => {
						const isActive = active === id;

						return (
							<button
								key={id}
								type="button"
								aria-current={isActive ? 'page' : undefined}
								onClick={() => setSearchParams({ tab: id }, { replace: true })}
								className={cn(
									'flex shrink-0 items-center gap-2.5 rounded-md px-3 py-2 text-sm transition-colors',
									'focus-visible:ring-ring focus-visible:ring-2 focus-visible:outline-none',
									'md:w-full md:justify-start',
									isActive
										? 'bg-muted text-foreground font-medium'
										: 'text-muted-foreground hover:bg-muted/60 hover:text-foreground'
								)}
							>
								<Icon className="size-4 shrink-0" />
								{t(labelKey)}
							</button>
						);
					})}
				</nav>
			</aside>

			<div className="min-w-0 flex-1">
				{active === 'account' && <UpdateProfileForm />}
				{active === 'password' && <UpdatePasswordProfileForm />}
				{active === 'notifications' && <ListingNotification />}
			</div>
		</div>
	);
}
