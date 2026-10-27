import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import versionService from '@/features/version/services/versionService.ts';
import { releaseNotesUrl } from '@/lib/links.ts';

/**
 * Pill on the right of a sidebar menu row: the version of the API that is running, linked to its
 * release notes. Its parent `SidebarMenuItem` positions it; nothing is shown until it is known.
 */
export default function SidebarVersion() {
	const { t } = useTranslation();
	const { data } = useQuery({
		queryKey: ['version'],
		queryFn: () => versionService.getVersion(),
		staleTime: Infinity,
		retry: false,
	});

	if (!data) return null;

	const label = /^\d/.test(data.version) ? `v${data.version}` : data.version;

	return (
		<a
			href={releaseNotesUrl(data.version)}
			target="_blank"
			rel="noopener noreferrer"
			title={t('layout.sidebar.release_notes')}
			className="border-sidebar-border bg-sidebar text-muted-foreground hover:border-sidebar-ring hover:text-sidebar-foreground focus-visible:ring-sidebar-ring absolute top-1/2 right-1.5 flex h-5 -translate-y-1/2 items-center rounded-full border px-2 font-mono text-[11px] leading-none tabular-nums transition-colors outline-none focus-visible:ring-2 group-data-[collapsible=icon]:hidden"
		>
			{label}
			<span className="sr-only">
				{' '}
				{t('layout.sidebar.release_notes')} {t('layout.sidebar.new_tab')}
			</span>
		</a>
	);
}
