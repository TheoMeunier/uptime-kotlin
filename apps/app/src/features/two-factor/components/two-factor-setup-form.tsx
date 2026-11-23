import { type FormEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/atoms/button.tsx';
import { Field, FieldDescription, FieldLabel } from '@/components/atoms/field.tsx';
import CopyableCode from '@/components/molecules/copyable-code.tsx';
import OneTimeCodeInput, { ONE_TIME_CODE_LENGTH } from '@/components/molecules/one-time-code-input.tsx';
import QrCode from '@/components/molecules/qr-code.tsx';
import TwoFactorCard from '@/features/two-factor/components/two-factor-card.tsx';
import type { TwoFactorSetup } from '@/features/two-factor/services/two-factor-service.ts';

interface TwoFactorSetupFormProps {
	setup: TwoFactorSetup;
	onConfirm: (code: string) => void;
	onCancel: () => void;
	isConfirming: boolean;
}

export default function TwoFactorSetupForm({ setup, onConfirm, onCancel, isConfirming }: TwoFactorSetupFormProps) {
	const { t } = useTranslation();
	const [code, setCode] = useState('');

	const submit = (event: FormEvent) => {
		event.preventDefault();
		onConfirm(code);
	};

	return (
		<form onSubmit={submit}>
			<TwoFactorCard
				enabled={false}
				footer={
					<>
						<Button type="submit" disabled={isConfirming || code.length !== ONE_TIME_CODE_LENGTH}>
							{isConfirming ? t('two_factor.setup.confirming') : t('two_factor.setup.confirm')}
						</Button>
						<Button type="button" variant="ghost" onClick={onCancel} disabled={isConfirming}>
							{t('button.cancel')}
						</Button>
					</>
				}
			>
				<div className="flex flex-col gap-4 sm:flex-row sm:items-start">
					<QrCode value={setup.otpauth_uri} label={t('two_factor.setup.qr_alt')} />
					<div className="flex min-w-0 flex-1 flex-col gap-3 text-sm">
						<p>{t('two_factor.setup.scan')}</p>
						<p className="text-muted-foreground">{t('two_factor.setup.bitwarden_hint')}</p>
						<div className="flex flex-col gap-1">
							<span className="text-muted-foreground text-xs">{t('two_factor.setup.manual_key')}</span>
							<CopyableCode value={setup.secret} />
						</div>
					</div>
				</div>
				<Field>
					<FieldLabel htmlFor="two_factor_setup_code">{t('two_factor.setup.code_label')}</FieldLabel>
					<OneTimeCodeInput id="two_factor_setup_code" value={code} onChange={setCode} required />
					<FieldDescription>{t('two_factor.setup.code_hint')}</FieldDescription>
				</Field>
			</TwoFactorCard>
		</form>
	);
}
