import type { StoreNotificationSchema } from "@/features/notifications/hooks/useNotificationForm.ts";
import api from "@/api/kyClient.ts";

const notificationService = {
  async storeNotification(data: StoreNotificationSchema) {
    await api
      .post("notifications/new", {
        body: JSON.stringify(data),
      })
      .json();
  },
};

export default notificationService;
