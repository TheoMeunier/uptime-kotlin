import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ShieldOff } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Field, FieldLabel } from '@/components/atoms/field.tsx';
import { Input } from '@/components/atoms/input.tsx';
import FormDialogue from '@/components/molecules/form-dialogue.tsx';
import { useDisableTwoFactor } from '@/features/two-factor/hooks/useTwoFactor.ts';

export default function DisableTwoFactorDialogue() {
	const { t } = useTranslation();
	const [password, setPassword] = useState('');
	const [code, setCode] = useState('');
	const { disable, isLoading } = useDisableTwoFactor();

	return (
		<FormDialogue
			trigger={
				<Button variant="outline" size="sm">
					<ShieldOff className="size-4" />
					{t('two_factor.disable.button')}
				</Button>
			}
			title={t('two_factor.disable.title')}
			description={t('two_factor.disable.description')}
			submitLabel={t('two_factor.disable.confirm')}
			pendingLabel={t('two_factor.disable.disabling')}
			destructive
			isPending={isLoading}
			canSubmit={password !== '' && code.trim() !== ''}
			onSubmit={() => disable({ password, code: code.trim() })}
			onClose={() => {
				setPassword('');
				setCode('');
			}}
		>
			<Field>
				<FieldLabel htmlFor="two_factor_password">{t('profile.label.current_password')}</FieldLabel>
				<Input
					id="two_factor_password"
					type="password"
					autoComplete="current-password"
					value={password}
					onChange={(event) => setPassword(event.target.value)}
					required
				/>
			</Field>
			<Field>
				<FieldLabel htmlFor="two_factor_disable_code">{t('two_factor.disable.code_label')}</FieldLabel>
				<Input
					id="two_factor_disable_code"
					autoComplete="one-time-code"
					autoCapitalize="none"
					spellCheck={false}
					placeholder="123456 / xxxxx-xxxxx"
					maxLength={32}
					className="font-mono"
					value={code}
					onChange={(event) => setCode(event.target.value)}
					required
				/>
			</Field>
		</FormDialogue>
	);
}
