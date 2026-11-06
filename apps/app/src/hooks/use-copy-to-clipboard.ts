import { useCallback, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';

export default function useCopyToClipboard(resetAfterMs = 1_500) {
	const { t } = useTranslation();
	const [copied, setCopied] = useState(false);
	const timer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

	useEffect(() => () => clearTimeout(timer.current), []);

	const copy = useCallback(
		async (value: string) => {
			try {
				await navigator.clipboard.writeText(value);
				setCopied(true);
				clearTimeout(timer.current);
				timer.current = setTimeout(() => setCopied(false), resetAfterMs);
				return true;
			} catch {
				toast.error(t('clipboard.failed'));
				return false;
			}
		},
		[resetAfterMs, t]
	);

	return { copied, copy };
}
