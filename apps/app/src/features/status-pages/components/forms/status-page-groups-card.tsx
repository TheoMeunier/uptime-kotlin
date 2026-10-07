import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import GroupsEditor from '@/features/status-pages/components/editor/groups-editor.tsx';
import type { EditorGroup, EditorProbe } from '@/features/status-pages/lib/editor.ts';

interface StatusPageGroupsCardProps {
	groups: EditorGroup[];
	probes: Map<string, EditorProbe>;
	onChange: (groups: EditorGroup[]) => void;
}

export default function StatusPageGroupsCard({ groups, probes, onChange }: StatusPageGroupsCardProps) {
	const { t } = useTranslation();

	return (
		<Card>
			<CardHeader>
				<CardTitle>{t('status_pages.form.layout_title')}</CardTitle>
				<CardDescription>{t('status_pages.form.layout_description')}</CardDescription>
			</CardHeader>
			<CardContent>
				<GroupsEditor groups={groups} probes={probes} onChange={onChange} />
			</CardContent>
		</Card>
	);
}
