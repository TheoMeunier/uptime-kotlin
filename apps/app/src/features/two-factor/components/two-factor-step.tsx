import { type FormEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ShieldCheck } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card';
import { Field, FieldDescription, FieldGroup, FieldLabel } from '@/components/atoms/field';
import { Input } from '@/components/atoms/input';
import { Button } from '@/components/atoms/button.tsx';
import OneTimeCodeInput from '@/components/molecules/one-time-code-input.tsx';

interface TwoFactorStepProps {
	onSubmit: (code: string) => void;
	onBack: () => void;
	isVerifying: boolean;
}

export default function TwoFactorStep({ onSubmit, onBack, isVerifying }: TwoFactorStepProps) {
	const { t } = useTranslation();
	const [useRecoveryCode, setUseRecoveryCode] = useState(false);
	const [code, setCode] = useState('');

	const send = (value: string) => {
		if (value.trim() && !isVerifying) onSubmit(value.trim());
	};

	const submit = (event: FormEvent) => {
		event.preventDefault();
		send(code);
	};

	const switchMode = () => {
		setUseRecoveryCode((current) => !current);
		setCode('');
	};

	return (
		<Card>
			<CardHeader>
				<span className="bg-muted mx-auto mb-2 flex size-10 items-center justify-center rounded-full">
					<ShieldCheck className="size-5" />
				</span>
				<CardTitle className="text-center text-xl">{t('two_factor.login.title')}</CardTitle>
				<CardDescription className="text-center">
					{useRecoveryCode ? t('two_factor.login.recovery_description') : t('two_factor.login.description')}
				</CardDescription>
			</CardHeader>
			<CardContent>
				<form onSubmit={submit}>
					<FieldGroup>
						{useRecoveryCode ? (
							<Field>
								<FieldLabel htmlFor="recovery_code">{t('two_factor.login.recovery_label')}</FieldLabel>
								<Input
									id="recovery_code"
									name="recovery_code"
									autoComplete="off"
									autoCapitalize="none"
									spellCheck={false}
									placeholder="xxxxx-xxxxx"
									maxLength={32}
									value={code}
									onChange={(event) => setCode(event.target.value)}
									autoFocus
									required
								/>
							</Field>
						) : (
							<Field>
								<FieldLabel htmlFor="totp">{t('two_factor.login.code_label')}</FieldLabel>
								<OneTimeCodeInput
									id="totp"
									name="totp"
									className="text-center text-lg"
									value={code}
									onChange={setCode}
									onComplete={send}
									autoFocus
									required
								/>
								<FieldDescription>{t('two_factor.login.code_hint')}</FieldDescription>
							</Field>
						)}
						<Field>
							<Button type="submit" disabled={isVerifying || code.trim() === ''} className="w-full">
								{isVerifying ? t('two_factor.login.verifying') : t('two_factor.login.verify')}
							</Button>
						</Field>
						<div className="flex flex-col items-center gap-1">
							<Button type="button" variant="link" size="sm" onClick={switchMode}>
								{useRecoveryCode ? t('two_factor.login.use_app') : t('two_factor.login.use_recovery')}
							</Button>
							<Button type="button" variant="link" size="sm" className="text-muted-foreground" onClick={onBack}>
								{t('two_factor.login.back')}
							</Button>
						</div>
					</FieldGroup>
				</form>
			</CardContent>
		</Card>
	);
}
