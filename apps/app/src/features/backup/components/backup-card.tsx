import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Download } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import { Field, FieldContent, FieldDescription, FieldGroup, FieldLabel } from '@/components/atoms/field.tsx';
import { Switch } from '@/components/atoms/switch.tsx';
import useDownloadBackup from '@/features/backup/hooks/useDownloadBackup.ts';
import type { SecretsMode } from '@/features/backup/schemas/backup.schema.ts';

export default function BackupCard() {
	const { t } = useTranslation();
	const [secrets, setSecrets] = useState<SecretsMode>('encrypted');
	const { downloadBackup, isDownloading } = useDownloadBackup();

	return (
		<Card>
			<CardHeader>
				<CardTitle>{t('backup.download.title')}</CardTitle>
				<CardDescription>{t('backup.download.description')}</CardDescription>
			</CardHeader>
			<CardContent>
				<FieldGroup>
					<Field orientation="horizontal">
						<Switch
							id="backup-plain-secrets"
							checked={secrets === 'plain'}
							onCheckedChange={(plain) => setSecrets(plain ? 'plain' : 'encrypted')}
						/>
						<FieldContent>
							<FieldLabel htmlFor="backup-plain-secrets">{t('backup.download.plain_label')}</FieldLabel>
							<FieldDescription>{t(`backup.download.secrets.${secrets}`)}</FieldDescription>
						</FieldContent>
					</Field>

					<div className="flex justify-end">
						<Button type="button" variant="outline" disabled={isDownloading} onClick={() => downloadBackup(secrets)}>
							<Download className="size-4" />
							{t(isDownloading ? 'button.exporting' : 'backup.download.button')}
						</Button>
					</div>
				</FieldGroup>
			</CardContent>
		</Card>
	);
}
