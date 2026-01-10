import UpdateProfileForm from '@/features/profile/components/forms/update-profile-form.tsx';
import UpdatePasswordProfileForm from '@/features/profile/components/forms/update-password-profile-form.tsx';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/atoms/tabs.tsx';
import { useTranslation } from 'react-i18next';

export default function Profile() {
	const { t } = useTranslation();

	return (
		<Tabs defaultValue="account" className="space-y-4">
			<TabsList className="max-w-[500px]">
				<TabsTrigger value="account">{t('profile.tabs.account')}</TabsTrigger>
				<TabsTrigger value="password">{t('profile.tabs.password')}</TabsTrigger>
			</TabsList>
			<TabsContent value="account">
				<UpdateProfileForm />
			</TabsContent>
			<TabsContent value="password">
				<UpdatePasswordProfileForm />
			</TabsContent>
		</Tabs>
	);
}
