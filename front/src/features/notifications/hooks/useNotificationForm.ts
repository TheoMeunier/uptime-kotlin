import { type SubmitHandler, useForm } from "react-hook-form";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import notificationService from "@/features/notifications/services/notification-service.ts";
import { useState } from "react";
import { toast } from "sonner";
import { data } from "react-router";
import { useTranslation } from "react-i18next";

const baseStoreNotificationSchema = z.object({
  name: z.string().min(3).max(255),
  is_default: z.boolean().optional(),
});

const discordNotificationSchema = baseStoreNotificationSchema.extend({
  notification_type: z.literal("DISCORD"),
  url_webhook: z.url(),
  name_reboot: z.string().min(3).max(255),
});

const teamsNotificationSchema = baseStoreNotificationSchema.extend({
  notification_type: z.literal("TEAMS"),
  url_webhook: z.url(),
  name_reboot: z.string().min(3).max(255),
});

const MailNotificationSchema = baseStoreNotificationSchema.extend({
  notification_type: z.literal("MAIL"),
  hostname: z.url(),
  port: z.number().min(1).max(65535),
  username: z.email().min(3).max(255),
  password: z.string().min(3).max(255),
  mail_from: z.email().min(3).max(255),
  mail_to: z.email().min(3).max(255),
});

export const storeNotificationSchema = z.discriminatedUnion(
  "notification_type",
  [discordNotificationSchema, MailNotificationSchema, teamsNotificationSchema],
);

export type StoreNotificationSchema = z.infer<typeof storeNotificationSchema>;

export default function useNotificationForm() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [openDialogue, setOpenDialogue] = useState(false);

  const form = useForm<StoreNotificationSchema>({
    resolver: zodResolver(storeNotificationSchema),
    defaultValues: {
      notification_type: "DISCORD",
    },
  });

  const mutation = useMutation({
    mutationFn: async (data: StoreNotificationSchema) => {
      return notificationService.storeNotification(data);
    },
    onSuccess: () => {
      queryClient
        .invalidateQueries({ queryKey: ["notifications"] })
        .then(() => {
          setOpenDialogue(false);
          toast.success(t("notifications.alerts.create", { data: data.name }));
        });
    },
  });

  const onSubmit: SubmitHandler<StoreNotificationSchema> = async (
    data: StoreNotificationSchema,
  ) => {
    mutation.mutate(data);
  };

  return {
    openDialogue,
    setOpenDialogue,
    form,
    onSubmit,
    isLoading: mutation.isPending,
    errors: form.formState.errors,
  };
}
