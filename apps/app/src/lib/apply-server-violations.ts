import type { FieldValues, Path, UseFormReturn } from 'react-hook-form';
import { ApiError } from '@/api/api-error.ts';

const toSnakeCase = (value: string) => value.replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`);

export function applyServerViolations<T extends FieldValues>(form: UseFormReturn<T>, error: unknown) {
	if (!(error instanceof ApiError)) return;

	const values = form.getValues() as Record<string, unknown>;

	for (const violation of error.violations) {
		const property = violation.field.split('.').pop() ?? '';
		const name = toSnakeCase(property);

		if (name in values) {
			form.setError(name as Path<T>, { type: 'server', message: violation.message });
		}
	}
}
