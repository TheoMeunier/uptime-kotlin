import UnsavedChangesGuard from '@/components/molecules/forms/unsaved-changes-guard.tsx';
import StatusPageGroupsCard from '@/features/status-pages/components/forms/status-page-groups-card.tsx';
import StatusPageSettingsCard from '@/features/status-pages/components/forms/status-page-settings-card.tsx';
import useEditorProbes from '@/features/status-pages/hooks/useEditorProbes.ts';
import useStatusPageForm from '@/features/status-pages/hooks/useStatusPageForm.ts';
import type { StatusPageDetail } from '@/features/status-pages/schemas/status-page.schema.ts';
import type { StatusPagePayload } from '@/features/status-pages/services/status-page-service.ts';

interface StatusPageFormProps {
	page?: StatusPageDetail;
	isSaving: boolean;
	onSubmit: (payload: StatusPagePayload) => Promise<{ id: string }>;
	onSaved?: (result: { id: string }) => void;
}

export default function StatusPageForm({ page, isSaving, onSubmit, onSaved }: StatusPageFormProps) {
	const form = useStatusPageForm(page);
	const probes = useEditorProbes(page);
	const isExisting = Boolean(page);

	return (
		<form
			onSubmit={form.handleSubmit(onSubmit, onSaved)}
			className="grid gap-4 lg:grid-cols-[minmax(0,22rem)_minmax(0,1fr)] lg:items-start"
		>
			<UnsavedChangesGuard when={form.isDirty && !isSaving} />

			<StatusPageSettingsCard
				form={form}
				isSaving={isSaving}
				canSave={!isExisting || form.isDirty}
				showUnsaved={isExisting && form.isDirty}
			/>

			<StatusPageGroupsCard groups={form.values.groups} probes={probes} onChange={form.setGroups} />
		</form>
	);
}
