import BackupCard from '@/features/backup/components/backup-card.tsx';
import RestoreCard from '@/features/backup/components/restore-card.tsx';

export default function BackupSettings() {
	return (
		<div className="flex flex-col gap-6">
			<BackupCard />
			<RestoreCard />
		</div>
	);
}
