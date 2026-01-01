import type {
  MultiSelectGroup,
  MultiSelectOption,
} from "@/components/atoms/multi-select.tsx";

export interface FieldConfig {
  name: string;
  label: string;
  input_type: string;
  default_value?: string | number | boolean | string[] | number[];
  placeholder?: string;
  description?: string;
  searchable?: boolean;
  closeOnSelect?: boolean;
  options?: MultiSelectOption[] | MultiSelectGroup[] | readonly string[];
}

export interface NotificationTypeConfig {
  label: string;
  icon: string;
  fields: FieldConfig[];
}

export type NotificationTypes = "discord" | "email" | "slack" | "sms";

export type NotificationConfig = Record<
  NotificationTypes,
  NotificationTypeConfig
>;
