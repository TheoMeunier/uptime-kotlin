import { useEffect, useMemo, useRef, useState } from 'react';
import { Link } from 'react-router';
import { useTranslation } from 'react-i18next';
import { Search, X } from 'lucide-react';

import {
	SidebarGroup,
	SidebarGroupLabel,
	SidebarInput,
	SidebarMenu,
	SidebarMenuItem,
} from '@/components/atoms/sidebar.tsx';
import { ScrollArea } from '@/components/atoms/scroll-area.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import type { ProbeListItem } from '@/features/probes/schemas/probe-response.schema.ts';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import { getStatusTokens } from '@/lib/status.ts';
import { createSearchMatcher } from '@/lib/search.ts';

const FILTER_THRESHOLD = 8;
const LIST_ID = 'sidebar-monitor-list';

function isTypingTarget(target: EventTarget | null): boolean {
	if (!(target instanceof HTMLElement)) return false;

	return target.isContentEditable || ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName);
}

interface SidebarMonitorsProps {
	probes: ProbeListItem[] | undefined;
	isLoading: boolean;
}

export default function SidebarMonitors({ probes, isLoading }: SidebarMonitorsProps) {
	const { t } = useTranslation();
	const [query, setQuery] = useState('');
	const inputRef = useRef<HTMLInputElement>(null);

	const total = probes?.length ?? 0;
	const showFilter = total >= FILTER_THRESHOLD;

	const matches = useMemo(() => createSearchMatcher(query), [query]);
	const visible = useMemo(
		() => (probes ?? []).filter((probe) => matches(probe.name, probe.description)),
		[probes, matches]
	);

	useEffect(() => {
		if (!showFilter) setQuery('');
	}, [showFilter]);

	useEffect(() => {
		if (!showFilter) return;

		const onKeyDown = (event: KeyboardEvent) => {
			if (event.key !== '/' || event.metaKey || event.ctrlKey || event.altKey) return;
			if (isTypingTarget(event.target)) return;

			event.preventDefault();
			inputRef.current?.focus();
		};

		window.addEventListener('keydown', onKeyDown);
		return () => window.removeEventListener('keydown', onKeyDown);
	}, [showFilter]);

	const isFiltering = query.trim().length > 0;

	return (
		<SidebarGroup className="flex min-h-0 flex-1 flex-col">
			<SidebarGroupLabel className="text-muted-foreground flex items-center justify-between px-2 py-1 text-xs font-semibold">
				<span>{t('layout.sidebar.monitors')}</span>
				{!isLoading && total > 0 && (
					<span className="tabular font-normal" aria-live="polite">
						{isFiltering ? t('layout.sidebar.search.count', { visible: visible.length, total }) : String(total)}
					</span>
				)}
			</SidebarGroupLabel>

			{showFilter && (
				<div role="search" className="relative px-1 pb-2">
					<Search className="text-muted-foreground pointer-events-none absolute top-1/2 left-3 size-3.5 -translate-y-1/2" />
					<SidebarInput
						ref={inputRef}
						type="text"
						value={query}
						onChange={(event) => setQuery(event.target.value)}
						onKeyDown={(event) => {
							if (event.key !== 'Escape') return;

							event.stopPropagation();
							if (query) setQuery('');
							else inputRef.current?.blur();
						}}
						placeholder={t('layout.sidebar.search.placeholder')}
						aria-label={t('layout.sidebar.search.label')}
						aria-controls={LIST_ID}
						className="h-8 pr-8 pl-8"
					/>
					{isFiltering && (
						<button
							type="button"
							onClick={() => {
								setQuery('');
								inputRef.current?.focus();
							}}
							aria-label={t('layout.sidebar.search.clear')}
							className="text-muted-foreground hover:text-foreground absolute top-1/2 right-3 -translate-y-1/2 rounded-sm"
						>
							<X className="size-3.5" />
						</button>
					)}
				</div>
			)}

			<ScrollArea className="min-h-0 flex-1 px-1 py-1">
				<SidebarMenu id={LIST_ID} className="gap-1">
					{isLoading
						? Array.from({ length: 3 }).map((_, index) => (
								<SidebarMenuItem key={index} className="flex items-center gap-3 p-3">
									<Skeleton className="h-4 w-4 rounded" />
									<Skeleton className="h-4 flex-1" />
								</SidebarMenuItem>
							))
						: visible.map((monitor) => {
								const tokens = getStatusTokens(monitor.status);
								const isDown = monitor.status !== ProbeStatusEnum.SUCCESS;

								return (
									<SidebarMenuItem key={monitor.id}>
										<Link
											to={`/monitors/${monitor.id}`}
											className="hover:bg-sidebar-accent group flex w-full items-center gap-3 rounded-lg p-3 text-left transition"
										>
											<span className="relative flex size-2.5 shrink-0">
												{isDown && (
													<span
														className={`absolute inline-flex h-full w-full rounded-full opacity-75 motion-safe:animate-ping ${tokens.solid}`}
													/>
												)}
												<span className={`relative inline-flex size-2.5 rounded-full ${tokens.solid}`} />
											</span>

											<span className="truncate text-sm font-medium">{monitor.name}</span>
										</Link>
									</SidebarMenuItem>
								);
							})}
				</SidebarMenu>

				{!isLoading && isFiltering && visible.length === 0 && (
					<p className="text-muted-foreground px-3 py-4 text-xs">
						{t('layout.sidebar.search.empty', { query: query.trim() })}
					</p>
				)}
			</ScrollArea>
		</SidebarGroup>
	);
}
