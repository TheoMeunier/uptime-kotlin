import { useTranslation } from 'react-i18next';
import { Globe, Lock } from 'lucide-react';
import { Badge } from '@/components/atoms/badge.tsx';
import { Label } from '@/components/atoms/label.tsx';
import { Switch } from '@/components/atoms/switch.tsx';
import { usePermissions } from '@/features/auth/lib/role.ts';
import {
	useDefaultStatusPageSettings,
	useUpdateDefaultStatusPage,
} from '@/features/settings/hooks/useDefaultStatusPage.ts';
import StatusPageLinkRow from '@/features/status-pages/components/listing/status-page-link-row.tsx';

export default function GlobalStatusPageRow() {
	const { t } = useTranslation();
	const { can } = usePermissions();
	const { data } = useDefaultStatusPageSettings();
	const { update, isLoading } = useUpdateDefaultStatusPage();
	const isPrivate = data?.is_private ?? false;

	return (
		<StatusPageLinkRow
			icon={<Globe className="size-4" />}
			title={t('status_pages.global.title')}
			badge={
				isPrivate && (
					<Badge variant="secondary" className="shrink-0 gap-1">
						<Lock className="size-3" />
						{t('status_pages.private')}
					</Badge>
				)
			}
			href="/status"
			meta={t('status_pages.global.description')}
			actions={
				can('status_pages.manage') &&
				data && (
					<div className="flex items-center gap-2" title={t('status_pages.global.private_description')}>
						<Switch
							id="global-status-private"
							checked={isPrivate}
							disabled={isLoading}
							onCheckedChange={(checked) => update(checked)}
						/>
						<Label htmlFor="global-status-private" className="text-muted-foreground text-sm font-normal">
							{t('status_pages.private')}
						</Label>
					</div>
				)
			}
		/>
	);
}
