import { useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { RefreshCw } from 'lucide-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { Button } from '@/components/atoms/button.tsx';
import probeService from '@/features/probes/services/probeService.ts';

const REFRESH_DELAYS_MS = [1_500, 4_000, 9_000];

export default function CheckProbeNowButton({ probeId, enabled }: { probeId: string; enabled: boolean }) {
	const { t } = useTranslation();
	const client = useQueryClient();
	const timers = useRef<ReturnType<typeof setTimeout>[]>([]);

	useEffect(() => {
		return () => {
			timers.current.forEach(clearTimeout);
			timers.current = [];
		};
	}, []);

	const refreshProbe = () => {
		void client.invalidateQueries({ queryKey: ['probe', probeId] });
		void client.invalidateQueries({ queryKey: ['probes'] });
	};

	const mutation = useMutation({
		mutationFn: async () => probeService.checkProbeNow(probeId),
		onSuccess: (response) => {
			if (response.status === 'already_running') {
				toast.info(t('monitors.alerts.check_now_already_running'));
			} else {
				toast.success(t('monitors.alerts.check_now'));
			}

			refreshProbe();
			timers.current.push(...REFRESH_DELAYS_MS.map((delay) => setTimeout(refreshProbe, delay)));
		},
	});

	return (
		<Button
			variant="outline"
			onClick={() => mutation.mutate()}
			disabled={!enabled || mutation.isPending}
			title={enabled ? undefined : t('monitors.description.check_now_disabled')}
		>
			<RefreshCw className={`mr-2 h-4 w-4 ${mutation.isPending ? 'animate-spin' : ''}`} />
			{t('button.actions.check_now')}
		</Button>
	);
}
