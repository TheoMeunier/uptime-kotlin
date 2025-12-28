import UpdateProfileForm from "@/features/profile/components/forms/update-profile-form.tsx";
import UpdatePasswordProfileForm from "@/features/profile/components/forms/update-password-profile-form.tsx";
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from "@/components/atoms/tabs.tsx";

export default function Profile() {
  return (
    <Tabs defaultValue="account" className="space-y-4">
      <TabsList className="max-w-[500px]">
        <TabsTrigger value="account">Account</TabsTrigger>
        <TabsTrigger value="password">Password</TabsTrigger>
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
