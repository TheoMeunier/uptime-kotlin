import type { MultiSelectGroup, MultiSelectOption } from '@/components/atoms/multi-select.tsx';

export interface FieldConfig {
	name: string;
	label: string;
	input_type: string;
	default_value?: string | number | boolean | string[] | number[];
	placeholder?: string;
	description?: string;
	min?: number;
	max?: number;
	searchable?: boolean;
	closeOnSelect?: boolean;
	options?: MultiSelectOption[] | MultiSelectGroup[] | readonly string[];
	// Shown only when editing an existing channel.
	update_only?: boolean;
}

export interface NotificationTypeConfig {
	label: string;
	icon: string;
	fields: FieldConfig[];
}

export type NotificationTypes = 'discord' | 'email' | 'slack' | 'sms';

export type NotificationConfig = Record<NotificationTypes, NotificationTypeConfig>;
