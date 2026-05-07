import UpdateProfileForm from '@/features/profile/components/forms/update-profile-form.tsx';
import UpdatePasswordProfileForm from '@/features/profile/components/forms/update-password-profile-form.tsx';
import { useTranslation } from 'react-i18next';
import { useState } from 'react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/atoms/button';
import { Bell, Lock, User } from 'lucide-react';
import ListingNotification from '@/features/notifications/components/listing-notification.tsx';

export default function Profile() {
	const { t } = useTranslation();
	const [activeTab, setActiveTab] = useState('account');

	const navItems = [
		{
			id: 'account',
			label: t('profile.tabs.account'),
			icon: User,
		},
		{
			id: 'password',
			label: t('profile.tabs.password'),
			icon: Lock,
		},
		{
			id: 'notifications',
			label: t('profile.tabs.notifications'),
			icon: Bell,
		},
	];

	return (
		<div className="flex min-h-screen">
			<aside className="w-64 border-r bg-background">
				<nav className="space-y-1 p-4">
					{navItems.map((item) => {
						const Icon = item.icon;
						return (
							<Button
								key={item.id}
								variant={activeTab === item.id ? 'secondary' : 'ghost'}
								className={cn('w-full justify-start font-normal', activeTab === item.id && 'bg-secondary font-medium')}
								onClick={() => setActiveTab(item.id)}
							>
								<Icon className="mr-3 h-4 w-4" />
								{item.label}
							</Button>
						);
					})}
				</nav>
			</aside>

			<main className="flex-1 p-8">
				{activeTab === 'account' && <UpdateProfileForm />}
				{activeTab === 'password' && <UpdatePasswordProfileForm />}
				{activeTab === 'notifications' && <ListingNotification />}
			</main>
		</div>
	);
}
