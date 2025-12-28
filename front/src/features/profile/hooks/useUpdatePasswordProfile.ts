import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import profileService from "@/features/profile/services/profileService.tsx";

const storeUpdatePasswordSchema = z
  .object({
    password: z.string().min(8),
    password_confirmation: z.string().min(8),
  })
  .refine((data) => data.password === data.password_confirmation, {
    message: "Passwords do not match",
    path: ["password_confirmation"],
  });

export type StoreUpdatePasswordSchemaType = z.infer<
  typeof storeUpdatePasswordSchema
>;

export default function useUpdatePasswordProfile() {
  const form = useForm<StoreUpdatePasswordSchemaType>({
    resolver: zodResolver(storeUpdatePasswordSchema),
  });

  const mutation = useMutation({
    mutationFn: async (data: StoreUpdatePasswordSchemaType) => {
      return profileService.updatePasswordProfile(data);
    },
  });

  const onSubmit: (data: StoreUpdatePasswordSchemaType) => void = async (
    data: StoreUpdatePasswordSchemaType,
  ) => {
    mutation.mutate(data);
  };

  return {
    form,
    onSubmit,
    isLoading: mutation.isPending,
    error: mutation.error,
  };
}
