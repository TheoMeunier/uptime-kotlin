import NotificationTypeEnum from "@/features/notifications/enums/notification-type-enum.ts";

const NOTIFICATION_FIELDS_CONFIG = {
  [NotificationTypeEnum.DISCORD]: [
    {
      name: "url_webhook",
      label: "Webhook Url",
      input_type: "text",
      placeholder: "https://discord.com/api/webhooks/...",
    },
    {
      name: "name_reboot",
      label: "Name reboot",
      input_type: "text",
      placeholder: "Uptime kotlin",
    },
  ],
  [NotificationTypeEnum.MAIL]: [
    {
      name: "hostname",
      label: "Hostname",
      input_type: "text",
      placeholder: "https://smtp.mailler.exemple",
    },
    {
      name: "port",
      label: "Port",
      input_type: "number",
      placeholder: "587",
    },
    {
      name: "username",
      label: "Username",
      input_type: "text",
      placeholder: "uptime-kotlin@exemple.com",
    },
    {
      name: "password",
      label: "Password",
      input_type: "password",
      placeholder: "********",
    },
    {
      name: "mail_from",
      label: "Address From",
      input_type: "email",
      placeholder: "uptime-kotlin@exemple.fr",
    },
    {
      name: "mail_to",
      label: "Address To",
      input_type: "email",
      placeholder: "uptime-kotlin@exemple.fr, uptime-kotlin2@exemple.fr",
    },
  ],
};

export default NOTIFICATION_FIELDS_CONFIG;
