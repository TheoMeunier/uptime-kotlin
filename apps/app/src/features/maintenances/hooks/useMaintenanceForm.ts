import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { MaintenanceRecurrenceEnum } from '@/features/maintenances/schemas/maintenance.schema.ts';
import { toDatetimeLocal } from '@/lib/datetime.ts';

export const storeMaintenanceSchema = z
	.object({
		title: z.string().min(3).max(255),
		description: z.string().max(2000).optional(),
		starts_at: z.string().min(1),
		duration_minutes: z.number().int().min(1),
		recurrence: z.enum(MaintenanceRecurrenceEnum),
		recurrence_until: z.string().optional(),
		timezone: z.string().min(1),
		active: z.boolean(),
		is_public: z.boolean(),
		probe_ids: z.array(z.uuid()),
	})
	.refine((data) => data.recurrence === 'ONCE' || !data.recurrence_until || data.recurrence_until > data.starts_at, {
		message: 'The recurrence must end after its first occurrence',
		path: ['recurrence_until'],
	});

export type StoreMaintenanceSchema = z.infer<typeof storeMaintenanceSchema>;

export default function useMaintenanceForm(defaultValues: Partial<StoreMaintenanceSchema> = {}) {
	const form = useForm<StoreMaintenanceSchema>({
		resolver: zodResolver(storeMaintenanceSchema),
		defaultValues: {
			title: '',
			description: '',
			starts_at: toDatetimeLocal(new Date()),
			duration_minutes: 60,
			recurrence: 'ONCE',
			recurrence_until: '',
			timezone: Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC',
			active: true,
			is_public: true,
			probe_ids: [],
			...defaultValues,
		},
	});

	return {
		form,
		errors: form.formState.errors,
	};
}
