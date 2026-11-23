import { useTranslation } from 'react-i18next';
import { Download } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import CopyButton from '@/components/molecules/copy-button.tsx';
import { downloadText } from '@/lib/http.ts';

interface CodeListProps {
	codes: string[];
	fileName: string;
	fileHeader?: string;
	copyLabel?: string;
	downloadLabel?: string;
}

export default function CodeList({ codes, fileName, fileHeader, copyLabel, downloadLabel }: CodeListProps) {
	const { t } = useTranslation();
	const content = codes.join('\n');

	return (
		<div className="flex flex-col gap-3">
			<ul className="bg-muted grid grid-cols-2 gap-x-6 gap-y-1 rounded-md p-4 font-mono text-sm">
				{codes.map((code) => (
					<li key={code}>{code}</li>
				))}
			</ul>
			<div className="flex flex-wrap gap-2">
				<CopyButton value={content} label={copyLabel ?? t('clipboard.copy')} variant="outline" />
				<Button
					type="button"
					variant="outline"
					size="sm"
					onClick={() => downloadText(fileHeader ? `${fileHeader}\n\n${content}\n` : `${content}\n`, fileName)}
				>
					<Download className="size-3.5" />
					{downloadLabel ?? t('button.download')}
				</Button>
			</div>
		</div>
	);
}
