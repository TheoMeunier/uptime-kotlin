import { type DragEvent, type KeyboardEvent, useId, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { FileText, UploadCloud, X } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { cn } from '@/lib/utils';

const BYTE_UNITS = ['byte', 'kilobyte', 'megabyte', 'gigabyte'] as const;

function formatSize(bytes: number, locale: string): string {
	const exponent = Math.min(Math.floor(Math.log(Math.max(bytes, 1)) / Math.log(1024)), BYTE_UNITS.length - 1);

	return new Intl.NumberFormat(locale, {
		style: 'unit',
		unit: BYTE_UNITS[exponent],
		unitDisplay: 'short',
		maximumFractionDigits: 1,
	}).format(bytes / 1024 ** exponent);
}

interface FileDropzoneProps {
	id?: string;
	file: File | null;
	onChange: (file: File | null) => void;
	accept?: string;
	hint?: string;
	disabled?: boolean;
	invalid?: boolean;
}

export default function FileDropzone({ id, file, onChange, accept, hint, disabled, invalid }: FileDropzoneProps) {
	const { t, i18n } = useTranslation();
	const generatedId = useId();
	const inputId = id ?? generatedId;
	const inputRef = useRef<HTMLInputElement>(null);
	const [isDragging, setIsDragging] = useState(false);

	const browse = () => {
		if (!disabled) inputRef.current?.click();
	};

	const select = (selected: File | undefined) => {
		if (inputRef.current) inputRef.current.value = '';
		if (selected) onChange(selected);
	};

	const onDrop = (event: DragEvent<HTMLDivElement>) => {
		event.preventDefault();
		setIsDragging(false);
		if (!disabled) select(event.dataTransfer.files[0]);
	};

	const onDragOver = (event: DragEvent<HTMLDivElement>) => {
		event.preventDefault();
		if (!disabled) setIsDragging(true);
	};

	const onKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
		if (event.key !== 'Enter' && event.key !== ' ') return;
		event.preventDefault();
		browse();
	};

	return (
		<>
			<input
				ref={inputRef}
				id={inputId}
				type="file"
				accept={accept}
				disabled={disabled}
				className="sr-only"
				tabIndex={-1}
				onChange={(event) => select(event.target.files?.[0])}
			/>

			{file ? (
				<div
					className={cn(
						'bg-muted/40 flex items-center gap-3 rounded-lg border px-4 py-3',
						invalid && 'border-destructive bg-destructive/5'
					)}
				>
					<div
						className={cn(
							'bg-background flex size-10 shrink-0 items-center justify-center rounded-md border',
							invalid ? 'text-destructive' : 'text-primary'
						)}
					>
						<FileText className="size-5" />
					</div>
					<div className="min-w-0 flex-1">
						<p className="truncate text-sm font-medium">{file.name}</p>
						<p className="text-muted-foreground text-xs">{formatSize(file.size, i18n.language)}</p>
					</div>
					<Button type="button" variant="ghost" size="sm" disabled={disabled} onClick={browse}>
						{t('file_dropzone.replace')}
					</Button>
					<Button
						type="button"
						variant="ghost"
						size="icon-sm"
						disabled={disabled}
						aria-label={t('file_dropzone.remove')}
						onClick={() => onChange(null)}
					>
						<X className="size-4" />
					</Button>
				</div>
			) : (
				<div
					role="button"
					tabIndex={disabled ? -1 : 0}
					aria-disabled={disabled}
					aria-labelledby={`${inputId}-prompt`}
					onClick={browse}
					onKeyDown={onKeyDown}
					onDragOver={onDragOver}
					onDragLeave={() => setIsDragging(false)}
					onDrop={onDrop}
					className={cn(
						'flex cursor-pointer flex-col items-center justify-center gap-3 rounded-lg border-2 border-dashed px-6 py-8 text-center transition-colors',
						'hover:border-primary/50 hover:bg-muted/40',
						'focus-visible:ring-ring/50 focus-visible:ring-[3px] focus-visible:outline-none',
						isDragging && 'border-primary bg-primary/5',
						invalid && 'border-destructive/60',
						disabled && 'pointer-events-none opacity-50'
					)}
				>
					<div className="bg-muted text-muted-foreground flex size-11 items-center justify-center rounded-full">
						<UploadCloud className="size-5" />
					</div>
					<div className="flex flex-col gap-1">
						<p id={`${inputId}-prompt`} className="text-sm">
							{t('file_dropzone.prompt')}{' '}
							<span className="text-primary font-medium underline-offset-4 hover:underline">
								{t('file_dropzone.browse')}
							</span>
						</p>
						{hint && <p className="text-muted-foreground text-xs">{hint}</p>}
					</div>
				</div>
			)}
		</>
	);
}
