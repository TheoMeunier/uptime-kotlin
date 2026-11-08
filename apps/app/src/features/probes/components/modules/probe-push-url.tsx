import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { DialogClose } from '@radix-ui/react-dialog';
import { ChevronDown, KeyRound, MoreHorizontal, RotateCcw } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/atoms/button.tsx';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
} from '@/components/atoms/dialog.tsx';
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuTrigger,
} from '@/components/atoms/dropdown-menu.tsx';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/atoms/tabs.tsx';
import CopyableCode from '@/components/molecules/copyable-code.tsx';
import Metric, { MetricRow } from '@/components/molecules/metric.tsx';
import probeService from '@/features/probes/services/probeService.ts';
import type { ProbePush } from '@/features/probes/schemas/probe-response.schema.ts';
import { formatDateTime, formatDuration } from '@/lib/datetime.ts';
import { cn } from '@/lib/utils';

interface ProbePushUrlProps {
	probeId: string;
	push: ProbePush;
	periodSeconds?: number;
	graceSeconds?: number;
}

function pushUrlFor(token: string): string {
	return `${window.location.origin}/api/push/${token}`;
}

function RegeneratePushTokenDialogue({
	probeId,
	open,
	setOpen,
}: {
	probeId: string;
	open: boolean;
	setOpen: (open: boolean) => void;
}) {
	const { t } = useTranslation();
	const client = useQueryClient();

	const mutation = useMutation({
		mutationFn: async () => probeService.regeneratePushToken(probeId),
		onSuccess: async () => {
			await client.invalidateQueries({ queryKey: ['probe', probeId] });
			toast.success(t('monitors.push.regenerated'));
			setOpen(false);
		},
	});

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogContent className="sm:max-w-md">
				<DialogHeader className="mt-2 mb-4 items-center text-center">
					<div className="bg-status-down-bg flex h-12 w-12 items-center justify-center rounded-full">
						<KeyRound className="text-status-down-fg h-6 w-6" />
					</div>
					<DialogTitle className="mt-4">{t('monitors.push.regenerate_title')}</DialogTitle>
					<DialogDescription className="text-muted-foreground mb-4 text-center text-sm">
						{t('monitors.push.regenerate_description')}
					</DialogDescription>
				</DialogHeader>

				<DialogFooter className="grid grid-cols-2 gap-2">
					<DialogClose asChild>
						<Button variant="outline" className="w-full" disabled={mutation.isPending}>
							{t('button.cancel')}
						</Button>
					</DialogClose>
					<Button
						variant="destructive"
						className="w-full"
						disabled={mutation.isPending}
						onClick={() => mutation.mutate()}
					>
						{t('monitors.push.regenerate')}
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}

function ago(t: (key: string, options?: Record<string, unknown>) => string, iso: string): string {
	const seconds = Math.max(0, Math.round((Date.now() - new Date(iso).getTime()) / 1000));
	return seconds < 60 ? t('monitors.push.just_now') : t('monitors.push.ago', { duration: formatDuration(seconds) });
}

export default function ProbePushUrl({ probeId, push, periodSeconds, graceSeconds }: ProbePushUrlProps) {
	const { t, i18n } = useTranslation();
	const [regenerateOpen, setRegenerateOpen] = useState(false);
	const [showExamples, setShowExamples] = useState(false);
	const url = push.token ? pushUrlFor(push.token) : null;
	const cronExample = `0 2 * * *  /usr/local/bin/backup.sh && curl -fsS -m 10 --retry 3 ${url} > /dev/null`;
	const failureExample = `/usr/local/bin/backup.sh || curl -fsS -m 10 "${url}?status=down&msg=backup%20failed"`;

	const lastPushDown = push.last_push_signal === 'DOWN';
	const lastPushDetail = push.last_push_at
		? [
				formatDateTime(push.last_push_at, i18n.language),
				lastPushDown ? t('monitors.push.failure_reported') : null,
				push.last_push_message,
			]
				.filter(Boolean)
				.join(' · ')
		: t('monitors.push.never');

	return (
		<Card>
			<CardHeader>
				<div className="flex items-start justify-between gap-4">
					<div className="min-w-0">
						<CardTitle className="text-xl">{t('monitors.push.title')}</CardTitle>
						<CardDescription className="mt-1">
							{url ? t('monitors.push.description') : t('monitors.push.token_hidden')}
						</CardDescription>
					</div>
					{url && (
						<>
							<DropdownMenu>
								<DropdownMenuTrigger asChild>
									<Button variant="ghost" size="icon" className="size-8" aria-label={t('button.actions.more')}>
										<MoreHorizontal className="size-4" />
									</Button>
								</DropdownMenuTrigger>
								<DropdownMenuContent align="end" className="w-52">
									<DropdownMenuItem variant="destructive" onSelect={() => setRegenerateOpen(true)}>
										<RotateCcw />
										{t('monitors.push.regenerate')}
									</DropdownMenuItem>
								</DropdownMenuContent>
							</DropdownMenu>
							<RegeneratePushTokenDialogue probeId={probeId} open={regenerateOpen} setOpen={setRegenerateOpen} />
						</>
					)}
				</div>
			</CardHeader>

			<CardContent className="flex flex-col gap-6">
				{url && <CopyableCode value={url} size="md" />}

				<MetricRow>
					<Metric
						variant="inline"
						label={t('monitors.push.last_call')}
						value={push.last_push_at ? ago(t, push.last_push_at) : t('monitors.push.never_short')}
						state={!push.last_push_at ? 'neutral' : lastPushDown ? 'down' : 'up'}
						description={lastPushDetail}
					/>
					<Metric
						variant="inline"
						label={t('monitors.push.expected_every')}
						value={formatDuration(periodSeconds ?? 0)}
						description={t('monitors.push.grace', { grace: formatDuration(graceSeconds ?? 0) })}
					/>
					<Metric variant="inline" label={t('monitors.push.calls_received')} value={push.push_count} />
				</MetricRow>

				{url && (
					<div className="border-t pt-4">
						<button
							type="button"
							className="text-muted-foreground hover:text-foreground flex items-center gap-1.5 text-sm font-medium transition-colors"
							aria-expanded={showExamples}
							onClick={() => setShowExamples((value) => !value)}
						>
							<ChevronDown className={cn('size-4 transition-transform', showExamples ? 'rotate-0' : '-rotate-90')} />
							{t('monitors.push.examples')}
						</button>

						{showExamples && (
							<Tabs defaultValue="cron" className="mt-3">
								<TabsList>
									<TabsTrigger value="cron">{t('monitors.push.example_cron')}</TabsTrigger>
									<TabsTrigger value="failure">{t('monitors.push.example_failure')}</TabsTrigger>
								</TabsList>
								<TabsContent value="cron">
									<CopyableCode value={cronExample} />
								</TabsContent>
								<TabsContent value="failure">
									<CopyableCode value={failureExample} />
								</TabsContent>
							</Tabs>
						)}
					</div>
				)}
			</CardContent>
		</Card>
	);
}
