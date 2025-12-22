export interface SelectOption {
  value: string;
  label: string;
}

export interface ValidationRule {
  required?: string | boolean;
  pattern?: {
    value: RegExp;
    message: string;
  };
  minLength?: {
    value: number;
    message: string;
  };
  maxLength?: {
    value: number;
    message: string;
  };
}

export interface FieldConfig {
  name: string;
  label: string;
  input_type: string;
  placeholder?: string;
  rules?: ValidationRule;
  options?: SelectOption[];
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
