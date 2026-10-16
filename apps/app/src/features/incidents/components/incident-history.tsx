import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import { CheckCheck, ShieldCheck } from 'lucide-react';
import { Badge } from '@/components/atoms/badge.tsx';
import { Button } from '@/components/atoms/button.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/atoms/table.tsx';
import type { IncidentPage } from '@/features/incidents/schemas/incident.schema.ts';
import IncidentNoteDialog from '@/features/incidents/components/incident-note-dialog.tsx';
import { formatDateTime, formatOutageDuration } from '@/lib/datetime.ts';

export default function IncidentHistory({
	data,
	isLoading,
	isFetching,
	onPageChange,
	showProbe = true,
}: {
	data?: IncidentPage;
	isLoading: boolean;
	isFetching: boolean;
	onPageChange: (page: number) => void;
	showProbe?: boolean;
}) {
	const { t, i18n } = useTranslation();

	if (isLoading) {
		return (
			<div className="space-y-3">
				{Array.from({ length: 5 }).map((_, index) => (
					<Skeleton key={index} className="h-6 w-full" />
				))}
			</div>
		);
	}

	if (!data || data.total === 0) {
		return (
			<div className="py-12 text-center">
				<div className="bg-muted mx-auto mb-4 flex size-14 items-center justify-center rounded-full">
					<ShieldCheck className="text-muted-foreground size-7" />
				</div>
				<h3 className="text-foreground mb-1 font-semibold">{t('incidents.empty.title')}</h3>
				<p className="text-muted-foreground text-sm">{t('incidents.empty.description')}</p>
			</div>
		);
	}

	const first = data.page * data.size + 1;
	const last = Math.min(data.total, first + data.items.length - 1);
	const hasNext = last < data.total;

	return (
		<div className={isFetching ? 'opacity-70 transition-opacity' : 'transition-opacity'}>
			<Table>
				<TableHeader>
					<TableRow>
						{showProbe && <TableHead>{t('incidents.table.probe')}</TableHead>}
						<TableHead>{t('incidents.table.started')}</TableHead>
						<TableHead>{t('incidents.table.ended')}</TableHead>
						<TableHead>{t('incidents.table.duration')}</TableHead>
						<TableHead>{t('incidents.table.cause')}</TableHead>
					</TableRow>
				</TableHeader>
				<TableBody>
					{data.items.map((incident) => (
						<TableRow key={incident.id}>
							{showProbe && (
								<TableCell className="font-medium">
									<Link to={`/monitors/${incident.probe_id}?tab=incidents`} className="hover:underline">
										{incident.probe_name}
									</Link>
								</TableCell>
							)}
							<TableCell className="tabular text-muted-foreground whitespace-nowrap">
								{formatDateTime(incident.started_at, i18n.language)}
							</TableCell>
							<TableCell className="tabular text-muted-foreground whitespace-nowrap">
								{incident.ended_at ? formatDateTime(incident.ended_at, i18n.language) : '—'}
								{incident.closed_reason === 'PROBE_DISABLED' && (
									<Badge variant="outline" className="ml-2">
										{t('incidents.closed_reason.PROBE_DISABLED')}
									</Badge>
								)}
							</TableCell>
							<TableCell className="tabular font-medium whitespace-nowrap">
								{formatOutageDuration(incident.duration_seconds ?? 0)}
								{incident.acknowledged_at && (
									<span
										className="text-muted-foreground ml-1.5 inline-flex align-middle"
										title={t('incidents.acknowledge.done', {
											time: formatDateTime(incident.acknowledged_at, i18n.language),
										})}
									>
										<CheckCheck className="size-3.5" />
									</span>
								)}
							</TableCell>
							<TableCell className="text-muted-foreground max-w-md text-xs">
								<div className="flex items-start gap-2">
									<div className="min-w-0 flex-1">
										<p className="truncate" title={incident.opening_message}>
											{incident.opening_message || '—'}
										</p>
										{incident.note && (
											<p
												className="text-foreground/80 mt-1 line-clamp-2 break-words whitespace-pre-line"
												title={incident.note}
											>
												{incident.note}
											</p>
										)}
									</div>
									<IncidentNoteDialog incident={incident} />
								</div>
							</TableCell>
						</TableRow>
					))}
				</TableBody>
			</Table>

			<div className="mt-4 flex items-center justify-between gap-4">
				<p className="text-muted-foreground tabular text-xs">
					{t('incidents.pagination.range', { from: first, to: last, total: data.total })}
				</p>
				<div className="flex gap-2">
					<Button variant="outline" size="sm" disabled={data.page === 0} onClick={() => onPageChange(data.page - 1)}>
						{t('incidents.pagination.previous')}
					</Button>
					<Button variant="outline" size="sm" disabled={!hasNext} onClick={() => onPageChange(data.page + 1)}>
						{t('incidents.pagination.next')}
					</Button>
				</div>
			</div>
		</div>
	);
}
