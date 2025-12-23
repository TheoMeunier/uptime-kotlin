import useNotifications from "@/features/notifications/hooks/useNotifications.ts";
import { Controller } from "react-hook-form";
import { Switch } from "@/components/atoms/switch.tsx";
import { Label } from "@/components/atoms/label";

interface FormSelectNotificationProps {
  form: any;
}

export default function FormSelectNotification({
  form,
}: FormSelectNotificationProps) {
  const { data, isLoading } = useNotifications();

  if (isLoading) return <div>Loading...</div>;

  return (
    <div className="space-y-4">
      {data?.map((notification) => (
        <Controller
          key={notification.id}
          control={form.control}
          name={`notifications`}
          render={({ field }) => {
            const isChecked = field.value?.includes(notification.id) || false;

            return (
              <div className="flex items-center gap-4">
                <Switch
                  id={notification.id}
                  checked={isChecked}
                  onCheckedChange={(checked) => {
                    const currentValue = field.value || [];
                    if (checked) {
                      field.onChange([...currentValue, notification.id]);
                    } else {
                      field.onChange(
                        currentValue.filter(
                          (id: string) => id !== notification.id,
                        ),
                      );
                    }
                  }}
                />
                <Label htmlFor={notification.id} className="cursor-pointer">
                  {notification.name}
                </Label>
              </div>
            );
          }}
        />
      ))}
    </div>
  );
}
