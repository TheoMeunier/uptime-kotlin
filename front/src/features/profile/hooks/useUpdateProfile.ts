import { type SubmitHandler, useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import profileService from "@/features/profile/services/profileService.tsx";
import { toast } from "sonner";
import authService from "@/features/auth/services/authServices.ts";
import authServices from "@/features/auth/services/authServices.ts";

const storeUpdateProfileSchema = z.object({
  name: z.string().min(3),
  email: z.email(),
});

export type StoreUpdateProfileSchemaType = z.infer<
  typeof storeUpdateProfileSchema
>;

export default function useUpdateProfile() {
  const user = authService.getUser();

  const form = useForm<StoreUpdateProfileSchemaType>({
    resolver: zodResolver(storeUpdateProfileSchema),
    defaultValues: {
      name: user.username,
      email: user.email,
    },
  });

  const mutation = useMutation({
    mutationFn: async (data: StoreUpdateProfileSchemaType) => {
      return profileService.updateProfile(data);
    },
    onSuccess: (_, data) => {
      authServices.saveUser(data.email, data.name);
      toast.success("Profile updated successfully");
    },
  });

  const onSubmit: SubmitHandler<StoreUpdateProfileSchemaType> = async (
    data: StoreUpdateProfileSchemaType,
  ) => {
    mutation.mutate(data);
  };

  return {
    form,
    onSubmit,
    isLoading: mutation.isPending,
    errors: form.formState.errors,
  };
}
