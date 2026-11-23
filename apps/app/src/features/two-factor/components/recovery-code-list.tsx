import { useTranslation } from 'react-i18next';
import CodeList from '@/components/molecules/code-list.tsx';

export default function RecoveryCodeList({ codes }: { codes: string[] }) {
	const { t } = useTranslation();

	return (
		<CodeList
			codes={codes}
			fileName="uptime-kotlin-recovery-codes.txt"
			fileHeader={t('two_factor.recovery.file_header', { host: window.location.host })}
			copyLabel={t('two_factor.recovery.copy_all')}
		/>
	);
}
