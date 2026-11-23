import { useTranslation } from 'react-i18next';
import { ShieldCheck } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import DisableTwoFactorDialogue from '@/features/two-factor/components/disable-two-factor-dialogue.tsx';
import RecoveryCodeList from '@/features/two-factor/components/recovery-code-list.tsx';
import RegenerateRecoveryCodesDialogue from '@/features/two-factor/components/regenerate-recovery-codes-dialogue.tsx';
import TwoFactorCard from '@/features/two-factor/components/two-factor-card.tsx';
import TwoFactorSetupForm from '@/features/two-factor/components/two-factor-setup-form.tsx';
import { useTwoFactorSetup, useTwoFactorStatus } from '@/features/two-factor/hooks/useTwoFactor.ts';

export default function TwoFactorSettings() {
	const { t, i18n } = useTranslation();
	const status = useTwoFactorStatus();
	const flow = useTwoFactorSetup();

	if (status.isPending) {
		return (
			<TwoFactorCard>
				<Skeleton className="h-10 w-48" />
			</TwoFactorCard>
		);
	}

	if (flow.recoveryCodes) {
		return (
			<TwoFactorCard
				enabled
				footer={<Button onClick={() => void flow.finish()}>{t('two_factor.recovery.done')}</Button>}
			>
				<p className="text-sm font-medium">{t('two_factor.recovery.title')}</p>
				<p className="text-muted-foreground text-sm">{t('two_factor.recovery.description')}</p>
				<RecoveryCodeList codes={flow.recoveryCodes} />
			</TwoFactorCard>
		);
	}

	if (flow.setup) {
		return (
			<TwoFactorSetupForm
				setup={flow.setup}
				onConfirm={flow.confirm}
				onCancel={flow.cancel}
				isConfirming={flow.isConfirming}
			/>
		);
	}

	if (status.data?.enabled) {
		const since =
			status.data.enabled_at &&
			new Date(status.data.enabled_at).toLocaleDateString(i18n.language, { dateStyle: 'long' });

		return (
			<TwoFactorCard
				enabled
				footer={
					<>
						<RegenerateRecoveryCodesDialogue />
						<DisableTwoFactorDialogue />
					</>
				}
			>
				<div className="flex flex-col gap-1 text-sm">
					{since && <p>{t('two_factor.status.enabled_since', { date: since })}</p>}
					<p className="text-muted-foreground">
						{t('two_factor.status.codes_remaining', { count: status.data.recovery_codes_remaining })}
					</p>
				</div>
			</TwoFactorCard>
		);
	}

	return (
		<TwoFactorCard
			enabled={false}
			footer={
				<Button onClick={flow.start} disabled={flow.isStarting}>
					<ShieldCheck className="size-4" />
					{flow.isStarting ? t('two_factor.setup.starting') : t('two_factor.setup.start')}
				</Button>
			}
		>
			<p className="text-muted-foreground text-sm">{t('two_factor.setup.intro')}</p>
		</TwoFactorCard>
	);
}
