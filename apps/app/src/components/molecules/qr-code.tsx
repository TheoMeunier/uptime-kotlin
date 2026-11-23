import { useEffect, useState } from 'react';
import QRCode from 'qrcode';
import { cn } from '@/lib/utils';

interface QrCodeProps {
	value: string;
	label: string;
	size?: number;
	className?: string;
}

export default function QrCode({ value, label, size = 208, className }: QrCodeProps) {
	const [dataUrl, setDataUrl] = useState<string | null>(null);

	useEffect(() => {
		let cancelled = false;

		QRCode.toDataURL(value, { errorCorrectionLevel: 'M', margin: 2, width: size })
			.then((url) => !cancelled && setDataUrl(url))
			.catch(() => !cancelled && setDataUrl(null));

		return () => {
			cancelled = true;
		};
	}, [value, size]);

	if (!dataUrl) {
		return (
			<div
				className={cn('bg-muted animate-pulse rounded-md', className)}
				style={{ width: size, height: size }}
				aria-hidden
			/>
		);
	}

	return (
		<img src={dataUrl} alt={label} width={size} height={size} className={cn('rounded-md border bg-white', className)} />
	);
}
