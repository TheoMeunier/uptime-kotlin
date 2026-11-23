import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { RefreshCw } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Field, FieldLabel } from '@/components/atoms/field.tsx';
import FormDialogue from '@/components/molecules/form-dialogue.tsx';
import OneTimeCodeInput, { ONE_TIME_CODE_LENGTH } from '@/components/molecules/one-time-code-input.tsx';
import RecoveryCodeList from '@/features/two-factor/components/recovery-code-list.tsx';
import { useRegenerateRecoveryCodes } from '@/features/two-factor/hooks/useTwoFactor.ts';

export default function RegenerateRecoveryCodesDialogue() {
	const { t } = useTranslation();
	const [code, setCode] = useState('');
	const { regenerate, isLoading, recoveryCodes, clear } = useRegenerateRecoveryCodes();

	return (
		<FormDialogue
			trigger={
				<Button variant="outline" size="sm">
					<RefreshCw className="size-4" />
					{t('two_factor.recovery.regenerate')}
				</Button>
			}
			title={t('two_factor.recovery.regenerate_title')}
			description={t('two_factor.recovery.regenerate_description')}
			submitLabel={t('two_factor.recovery.regenerate')}
			pendingLabel={t('two_factor.recovery.regenerating')}
			isPending={isLoading}
			canSubmit={code.length === ONE_TIME_CODE_LENGTH}
			onSubmit={() => regenerate(code)}
			result={recoveryCodes && <RecoveryCodeList codes={recoveryCodes} />}
			resultDescription={t('two_factor.recovery.description')}
			closeLabel={t('two_factor.recovery.done')}
			onClose={() => {
				setCode('');
				clear();
			}}
		>
			<Field>
				<FieldLabel htmlFor="two_factor_regenerate_code">{t('two_factor.setup.code_label')}</FieldLabel>
				<OneTimeCodeInput id="two_factor_regenerate_code" value={code} onChange={setCode} required />
			</Field>
		</FormDialogue>
	);
}
