import { useTranslation } from 'react-i18next';
import { formatDayLong, formatTime } from '@/lib/datetime.ts';
import { BUCKET_TONE, bucketState, bucketUptime, formatUptime } from '@/features/status-pages/lib/uptime-period.ts';
import type { UptimeBucket } from '@/features/status-pages/schemas/status-uptime.schema.ts';

export default function UptimeBucketTooltip({ bucket, daily }: { bucket: UptimeBucket; daily: boolean }) {
	const { t, i18n } = useTranslation();
	const state = bucketState(bucket);
	const uptime = bucketUptime(bucket);

	return (
		<div className="space-y-2">
			<h4 className={`text-sm font-semibold ${BUCKET_TONE[state].text}`}>
				{t(`pages.status_page.uptime.state.${state}`)}
			</h4>

			{uptime != null && (
				<p className="text-sm">
					<span className="tabular font-medium">{formatUptime(uptime)}</span>{' '}
					<span className="text-muted-foreground">{t('pages.status_page.uptime.checks', { count: bucket.total })}</span>
				</p>
			)}

			{bucket.failures > 0 && (
				<p className="text-status-down-fg text-xs">
					{t('pages.status_page.uptime.failures', { count: bucket.failures })}
				</p>
			)}

			{bucket.maintenance > 0 && (
				<p className="text-status-maintenance-fg text-xs">
					{t('pages.status_page.uptime.maintenance', { count: bucket.maintenance })}
				</p>
			)}

			<div className="border-border text-muted-foreground tabular border-t pt-2 text-xs">
				{formatDayLong(bucket.starts_at, i18n.language)}
				{!daily && (
					<>
						{', '}
						{formatTime(bucket.starts_at, i18n.language)} – {formatTime(bucket.ends_at, i18n.language)}
					</>
				)}
			</div>
		</div>
	);
}
