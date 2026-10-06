import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import probeService from '@/features/probes/services/probeService.ts';
import type { ProbeStatusShowResponse } from '@/features/probes/schemas/probe-response.schema.ts';
import { Card, CardContent, CardDescription, CardTitle } from '@/components/atoms/card.tsx';
import ProbeMonitorChartBar from '@/features/probes/components/modules/probe-monitor-chart-bar.tsx';
import ProbeStatus from '@/features/probes/components/modules/probe-status.tsx';
import { Activity, Clock, LayoutGrid, LayoutList } from 'lucide-react';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import { Badge } from '@/components/atoms/badge.tsx';
import { useTranslation } from 'react-i18next';
import { summarizeAttention, uptimeState } from '@/lib/status.ts';
import useTabStatus from '@/hooks/use-tab-status.ts';
import ErrorState from '@/components/molecules/error-state.tsx';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import MaintenanceBadge from '@/features/maintenances/components/maintenance-badge.tsx';
import { formatTime } from '@/lib/datetime.ts';
import LanguageToggle from '@/components/molecules/language-toggle.tsx';
import { ToggleGroup, ToggleGroupItem } from '@/components/atoms/toggle-group.tsx';

type StatusLayout = 'grid' | 'list';

const LAYOUT_STORAGE_KEY = 'uptime-kotlin.status-layout';

/* Choix propre a chaque visiteur : la page est publique, il n'y a pas de compte ou le ranger. */
function readStoredLayout(): StatusLayout {
	try {
		return localStorage.getItem(LAYOUT_STORAGE_KEY) === 'list' ? 'list' : 'grid';
	} catch {
		return 'grid';
	}
}

function storeLayout(layout: StatusLayout) {
	try {
		localStorage.setItem(LAYOUT_STORAGE_KEY, layout);
	} catch {
		/* Stockage indisponible (navigation privee) : le choix vaut pour la session en cours. */
	}
}

type StatusItem = ProbeStatusShowResponse[number];

/* Grille et liste prennent la meme lecture des 30 jours, sinon un meme service changerait de couleur. */
function uptimeTone(value: number) {
	const state = uptimeState(value);

	if (state === 'down') return 'text-status-down-fg';
	if (state === 'degraded') return 'text-status-degraded-fg';

	return 'text-foreground';
}

export default function ProbesStatus() {
	const { t, i18n } = useTranslation();
	const [layout, setLayout] = useState<StatusLayout>(readStoredLayout);
	const isList = layout === 'list';
	const { data, isLoading, isError, refetch } = useQuery({
		queryKey: ['probes-status'],
		queryFn: async () => {
			return probeService.getProbesStatus();
		},
		refetchInterval: 120000,
	});

	const attention = useMemo(
		() => summarizeAttention(data?.filter((item) => !item.maintenance).map((item) => item.probe.status)),
		[data]
	);

	useTabStatus({ ...attention, page: t('pages.status_page.title') });

	if (isLoading) return <ProbesStatusSkeleton layout={layout} />;

	if (isError) {
		return (
			<div className="bg-background min-h-screen">
				<div className="mx-auto w-full max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
					<ErrorState onRetry={() => refetch()} />
				</div>
			</div>
		);
	}

	const items = data ?? [];
	const watched = items.filter((item) => !item.maintenance);
	const maintenanceCount = items.length - watched.length;
	const total = watched.length;
	const downCount = watched.filter((item) => item.probe.status === ProbeStatusEnum.FAILURE).length;
	const degradedCount = watched.filter((item) => item.probe.status === ProbeStatusEnum.WARNING).length;

	const summary =
		downCount > 0
			? {
					dot: 'bg-status-down',
					tone: 'text-status-down-fg',
					label: t('pages.status_page.verdict.down', { count: downCount, total }),
				}
			: degradedCount > 0
				? {
						dot: 'bg-status-degraded',
						tone: 'text-status-degraded-fg',
						label: t('pages.status_page.verdict.degraded', { count: degradedCount, total }),
					}
				: {
						dot: 'bg-status-up',
						tone: 'text-status-up-fg',
						label: t('pages.status_page.verdict.operational', { count: total }),
					};

	return (
		<div className="min-h-screen bg-background">
			<div className="bg-card border-b">
				<div className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
					<div className="flex items-center gap-3 mb-3">
						<div className="p-2 bg-primary/10 rounded-lg shrink-0">
							<Activity className="h-6 w-6 text-primary" />
						</div>
						<div>
							<h1 className="text-foreground text-2xl font-semibold tracking-tight sm:text-3xl">
								{t('pages.status_page.title')}
							</h1>
							<p className="text-sm sm:text-base text-muted-foreground mt-1">{t('pages.status_page.subtitle')}</p>
						</div>

						{/* The status page is public and has no app chrome: the selector has to live here. */}
						<div className="ml-auto flex items-center gap-1 self-start">
							<ToggleGroup
								type="single"
								spacing={1}
								value={layout}
								onValueChange={(value) => {
									if (value !== 'grid' && value !== 'list') return;
									setLayout(value);
									storeLayout(value);
								}}
								aria-label={t('pages.status_page.layout.label')}
								/* Sous md les deux dispositions donnent la meme colonne : inutile d'encombrer l'en-tete. */
								className="bg-muted/60 hidden rounded-lg p-1 md:flex"
							>
								<ToggleGroupItem
									value="grid"
									aria-label={t('pages.status_page.layout.grid')}
									title={t('pages.status_page.layout.grid')}
									className="data-[state=on]:bg-background data-[state=on]:text-foreground data-[state=on]:shadow-sm h-7 w-7 min-w-0 px-0"
								>
									<LayoutGrid className="size-4" />
								</ToggleGroupItem>
								<ToggleGroupItem
									value="list"
									aria-label={t('pages.status_page.layout.list')}
									title={t('pages.status_page.layout.list')}
									className="data-[state=on]:bg-background data-[state=on]:text-foreground data-[state=on]:shadow-sm h-7 w-7 min-w-0 px-0"
								>
									<LayoutList className="size-4" />
								</ToggleGroupItem>
							</ToggleGroup>
							<LanguageToggle />
						</div>
					</div>

					<div className="text-muted-foreground flex flex-wrap items-center gap-x-3 gap-y-1 text-sm">
						<span className="flex items-center gap-1.5 font-medium">
							<span className={`size-2 shrink-0 rounded-full ${summary.dot}`} />
							<span className={summary.tone}>{summary.label}</span>
						</span>

						{maintenanceCount > 0 && (
							<span className="text-status-maintenance-fg flex items-center gap-1.5">
								<span className="bg-status-maintenance size-2 shrink-0 rounded-full" />
								{t('pages.status_page.verdict.maintenance', { count: maintenanceCount })}
							</span>
						)}

						<span className="flex items-center gap-1.5">
							<Clock className="h-4 w-4 shrink-0" />
							<span className="tabular">
								{t('pages.status_page.description.last_update')}
								{formatTime(new Date(), i18n.language, { withSeconds: true })}
							</span>
						</span>

						<Badge variant="outline">{t('pages.status_page.description.automatic_refresh')} 2min</Badge>
					</div>
				</div>
			</div>

			<div className="mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 sm:py-12 lg:px-8">
				{isList ? (
					items.length > 0 && <StatusList items={items} />
				) : (
					<div className="grid grid-cols-1 gap-4 sm:gap-6 md:grid-cols-2 xl:grid-cols-3">
						{items.map((item) => (
							<StatusGridCard key={item.probe.id} item={item} />
						))}
					</div>
				)}

				{data?.length === 0 && (
					<Card className="border-border bg-card">
						<CardContent className="text-center py-16">
							<div className="p-4 bg-muted rounded-full w-16 h-16 mx-auto mb-4 flex items-center justify-center">
								<Activity className="h-8 w-8 text-muted-foreground" />
							</div>
							<h3 className="text-foreground mb-2 text-lg font-semibold">{t('pages.status_page.empty.title')}</h3>
							<p className="text-muted-foreground">{t('pages.status_page.empty.description')}</p>
						</CardContent>
					</Card>
				)}
			</div>
		</div>
	);
}

function StatusGridCard({ item }: { item: StatusItem }) {
	const { t } = useTranslation();

	return (
		<Card className="border-border bg-card hover:border-muted-foreground/30 transition-colors">
			<CardContent>
				<div>
					<div className="flex flex-wrap justify-between items-start gap-2">
						<div className="flex items-center min-w-0">
							<div className="min-w-0">
								<CardTitle className="text-base sm:text-lg font-semibold text-foreground truncate">
									{item.probe.name}
								</CardTitle>
								<CardDescription className="text-muted-foreground text-xs sm:text-sm truncate">
									{item.probe.url}
								</CardDescription>
							</div>
						</div>
						{item.maintenance ? (
							<MaintenanceBadge current={item.maintenance} size="sm" />
						) : (
							<ProbeStatus status={item.probe.status} size="sm" />
						)}
					</div>
				</div>

				<ProbeMonitorChartBar monitors={item.monitors} probeStatus={item.probe.status} barCount={30} />

				<div className="text-muted-foreground mt-1 flex justify-between text-xs">
					<span>{t('monitors.description.one_hour_ago')}</span>
					<span>{t('monitors.description.now')}</span>
				</div>

				{(item.uptimes || item.down_duration || item.next_maintenance) && (
					<div className="border-border mt-4 flex flex-wrap items-center justify-between gap-x-3 gap-y-1 border-t pt-3 text-xs">
						{item.uptimes ? (
							<span className="text-muted-foreground">
								{t('pages.status_page.uptime_30d')}{' '}
								<span className={`tabular font-semibold ${uptimeTone(item.uptimes.d30)}`}>
									{item.uptimes.d30.toFixed(2)}%
								</span>
							</span>
						) : (
							<span />
						)}

						{item.down_duration && !item.maintenance && (
							<span className="text-status-down-fg tabular font-medium">
								{t('pages.status_page.down_for', { duration: item.down_duration })}
							</span>
						)}

						{item.maintenance_duration && (
							<span className="text-muted-foreground tabular">
								{t('pages.status_page.planned_downtime', { duration: item.maintenance_duration })}
							</span>
						)}

						{!item.maintenance && item.next_maintenance && <MaintenanceBadge next={item.next_maintenance} size="sm" />}
					</div>
				)}
			</CardContent>
		</Card>
	);
}

/*
 * Une carte basse par service, avec des colonnes alignees d'une carte a l'autre. La legende du temps
 * est donnee une fois dans l'en-tete plutot que repetee sous chaque carte.
 */
const LIST_COLUMNS = 'lg:grid lg:grid-cols-[minmax(0,16rem)_minmax(0,1fr)] lg:items-center lg:gap-8';
const LIST_BAR_COUNT = 60;

/*
 * Liseré de couleur seulement quand il y a quelque chose a regarder : un service sain reste neutre,
 * comme partout ailleurs dans l'application (voir lib/status.ts).
 */
function attentionAccent(item: StatusItem): string | null {
	if (item.maintenance) return 'bg-status-maintenance';
	if (item.probe.status === ProbeStatusEnum.FAILURE) return 'bg-status-down';
	if (item.probe.status === ProbeStatusEnum.WARNING) return 'bg-status-degraded';

	return null;
}

/* Le schema https:// n'apprend rien au visiteur et mange la moitie de la colonne. */
function displayTarget(url: string) {
	return url.replace(/^https?:\/\//, '').replace(/\/$/, '');
}

function StatusList({ items }: { items: StatusItem[] }) {
	const { t } = useTranslation();

	return (
		<div>
			{/* Bordure transparente : meme largeur utile que les cartes, donc colonnes alignees au pixel. */}
			<div
				className={`text-muted-foreground hidden border-x border-transparent px-5 pb-2 text-xs font-medium ${LIST_COLUMNS}`}
			>
				<span>{t('pages.status_page.layout.columns.service')}</span>
				<span className="flex justify-between font-normal">
					<span>{t('monitors.description.one_hour_ago')}</span>
					<span>{t('monitors.description.now')}</span>
				</span>
			</div>

			<ul className="flex flex-col gap-2.5">
				{items.map((item) => (
					<StatusListRow key={item.probe.id} item={item} />
				))}
			</ul>
		</div>
	);
}

/*
 * Pas de badge ni de chiffre : les barres portent l'historique et le liseré signale ce qui demande
 * de l'attention.
 */
function StatusListRow({ item }: { item: StatusItem }) {
	const { t } = useTranslation();

	const accent = attentionAccent(item);
	const isDown = !item.maintenance && item.probe.status === ProbeStatusEnum.FAILURE;
	const showDownFor = Boolean(item.down_duration && !item.maintenance);

	return (
		<li
			className={`bg-card text-card-foreground relative flex flex-col gap-3 overflow-hidden rounded-lg border px-4 py-4 transition-colors sm:px-5 ${
				isDown ? 'border-status-down/40' : 'border-border hover:border-muted-foreground/30'
			} ${LIST_COLUMNS}`}
		>
			{accent && <span aria-hidden className={`absolute inset-y-0 left-0 w-[3px] ${accent}`} />}

			<div className="min-w-0">
				<p className="text-foreground truncate text-base font-semibold" title={item.probe.name}>
					{item.probe.name}
				</p>
				<p className="text-muted-foreground truncate text-sm" title={item.probe.url}>
					{showDownFor ? (
						<span className="text-status-down-fg tabular font-medium">
							{t('pages.status_page.down_for', { duration: item.down_duration })}
						</span>
					) : (
						displayTarget(item.probe.url)
					)}
				</p>
			</div>

			<ProbeMonitorChartBar
				monitors={item.monitors}
				probeStatus={item.probe.status}
				barCount={LIST_BAR_COUNT}
				compact
			/>
		</li>
	);
}

function ProbesStatusSkeleton({ layout }: { layout: StatusLayout }) {
	const BAR_COUNT = 30;
	const CARD_COUNT = 6;

	return (
		<div className="bg-background min-h-screen">
			<div className="bg-card border-b">
				<div className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
					<div className="mb-3 flex items-center gap-3">
						<Skeleton className="h-10 w-10 shrink-0 rounded-lg" />
						<div className="flex flex-col gap-2">
							<Skeleton className="h-7 w-56" />
							<Skeleton className="h-3.5 w-72" />
						</div>
					</div>
					<div className="flex items-center gap-3">
						<div className="flex items-center gap-1.5">
							<Skeleton className="size-2 rounded-full" />
							<Skeleton className="h-3 w-40" />
						</div>
						<div className="flex items-center gap-1.5">
							<Skeleton className="h-4 w-4 rounded" />
							<Skeleton className="h-3 w-36" />
						</div>
						<Skeleton className="h-6 w-28 rounded-full" />
					</div>
				</div>
			</div>

			<div className="mx-auto w-full max-w-7xl px-4 py-8 sm:px-6 sm:py-12 lg:px-8">
				{layout === 'list' ? (
					<div>
						<div className="hidden h-6 lg:block" />
						<div className="flex flex-col gap-2.5">
							{Array.from({ length: CARD_COUNT }).map((_, i) => (
								<div
									key={i}
									className={`bg-card text-card-foreground border-border rounded-lg border flex flex-col gap-3 px-4 py-4 sm:px-5 ${LIST_COLUMNS}`}
								>
									<div className="flex flex-col gap-1.5">
										<Skeleton className="h-5 w-36" />
										<Skeleton className="h-3.5 w-48" />
									</div>
									<Skeleton className="h-6 w-full rounded-[2px]" />
								</div>
							))}
						</div>
					</div>
				) : (
					<div className="grid grid-cols-1 gap-4 sm:gap-6 md:grid-cols-2 xl:grid-cols-3">
						{Array.from({ length: CARD_COUNT }).map((_, i) => (
							<Card key={i} className="border-border bg-card">
								<CardContent>
									<div className="flex items-start justify-between gap-2">
										<div className="flex flex-col gap-1.5">
											<Skeleton className="h-4 w-32" />
											<Skeleton className="h-3 w-48" />
										</div>
										<Skeleton className="h-6 w-20 shrink-0 rounded-full" />
									</div>

									<div className="my-3 flex h-8 w-full items-end gap-[3px]">
										{Array.from({ length: BAR_COUNT }).map((_, j) => (
											<Skeleton key={j} className="h-8 flex-1 rounded-[2px]" />
										))}
									</div>

									<div className="mt-1 flex justify-between">
										<Skeleton className="h-2.5 w-16" />
										<Skeleton className="h-2.5 w-10" />
									</div>

									<div className="border-border mt-4 flex items-center justify-between border-t pt-3">
										<Skeleton className="h-3 w-32" />
									</div>
								</CardContent>
							</Card>
						))}
					</div>
				)}
			</div>
		</div>
	);
}
