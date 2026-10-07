import * as React from 'react';
import { cn } from '@/lib/utils';

interface PrefixedInputProps extends Omit<React.ComponentProps<'input'>, 'prefix'> {
	prefix: React.ReactNode;
	containerClassName?: string;
}

/* A text input with a fixed, non-editable prefix drawn inside the same border (URL paths, units…). */
export default function PrefixedInput({ prefix, className, containerClassName, ...props }: PrefixedInputProps) {
	return (
		<div
			className={cn(
				'border-input dark:bg-input/30 flex h-9 w-full min-w-0 items-center rounded-md border shadow-xs transition-[color,box-shadow]',
				'focus-within:border-ring focus-within:ring-ring/50 focus-within:ring-[3px]',
				'has-[input[aria-invalid=true]]:border-destructive has-[input[aria-invalid=true]]:ring-destructive/20',
				containerClassName
			)}
		>
			<span className="text-muted-foreground shrink-0 pl-3 text-sm whitespace-nowrap select-none">{prefix}</span>
			<input
				data-slot="input"
				className={cn(
					'placeholder:text-muted-foreground h-full min-w-0 flex-1 bg-transparent pr-3 text-base outline-none md:text-sm',
					className
				)}
				{...props}
			/>
		</div>
	);
}
