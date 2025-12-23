import { useQuery } from "@tanstack/react-query";
import notificationService from "@/features/notifications/services/notification-service.ts";

export default function useNotifications() {
  const { data, isLoading } = useQuery({
    queryKey: ["notifications"],
    queryFn: async () => {
      return notificationService.getNotifications();
    },
  });

  return {
    data: data,
    isLoading: isLoading,
  };
}
