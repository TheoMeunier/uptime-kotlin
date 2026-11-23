import type { ComponentProps } from 'react';
import { Input } from '@/components/atoms/input.tsx';
import { cn } from '@/lib/utils';

export const ONE_TIME_CODE_LENGTH = 6;

type OneTimeCodeInputProps = Omit<ComponentProps<typeof Input>, 'value' | 'onChange' | 'type'> & {
	value: string;
	onChange: (value: string) => void;
	onComplete?: (code: string) => void;
	length?: number;
};

export default function OneTimeCodeInput({
	value,
	onChange,
	onComplete,
	length = ONE_TIME_CODE_LENGTH,
	className,
	...props
}: OneTimeCodeInputProps) {
	const handleChange = (raw: string) => {
		const digits = raw.replace(/\D/g, '').slice(0, length);
		onChange(digits);
		if (digits.length === length) onComplete?.(digits);
	};

	return (
		<Input
			type="text"
			inputMode="numeric"
			autoComplete="one-time-code"
			pattern="[0-9]*"
			maxLength={length}
			placeholder={'0'.repeat(length)}
			className={cn('font-mono tracking-[0.3em]', className)}
			value={value}
			onChange={(event) => handleChange(event.target.value)}
			{...props}
		/>
	);
}
