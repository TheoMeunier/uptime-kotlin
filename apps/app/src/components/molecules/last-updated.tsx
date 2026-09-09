import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

/**
 * On a monitoring tool, how fresh the data is *is* part of the data: a dashboard that silently
 * shows five-minute-old numbers is worse than one that admits it.
 */
export default function LastUpdated({ at }: { at: number | undefined }) {
	const { t } = useTranslation();
	const [, forceRender] = useState(0);

	useEffect(() => {
		const id = setInterval(() => forceRender((n) => n + 1), 10_000);
		return () => clearInterval(id);
	}, []);

	if (!at) return null;

	const seconds = Math.max(0, Math.round((Date.now() - at) / 1000));
	const label =
		seconds < 60
			? t('layout.updated.seconds', { count: seconds })
			: t('layout.updated.minutes', { count: Math.floor(seconds / 60) });

	return <span className="text-muted-foreground hidden text-xs sm:inline">{label}</span>;
}
