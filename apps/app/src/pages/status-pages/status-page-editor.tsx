import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { useTranslation } from 'react-i18next';
import ErrorState from '@/components/molecules/error-state.tsx';
import StatusPageEditorHeader from '@/features/status-pages/components/forms/status-page-editor-header.tsx';
import StatusPageForm from '@/features/status-pages/components/forms/status-page-form.tsx';
import StatusPageFormSkeleton from '@/features/status-pages/components/forms/status-page-form-skeleton.tsx';
import { useSaveStatusPage, useStatusPage } from '@/features/status-pages/hooks/useStatusPages.ts';

export default function StatusPageEditor() {
	const { t } = useTranslation();
	const navigate = useNavigate();
	const { statusPageId } = useParams();
	const isCreate = !statusPageId;

	const { data: page, isLoading, isError, refetch } = useStatusPage(statusPageId);
	const save = useSaveStatusPage(statusPageId);

	/* Navigating from an effect: by then the form is clean and the unsaved-changes guard lets it through. */
	const [createdId, setCreatedId] = useState<string>();
	useEffect(() => {
		if (createdId) navigate(`/status-pages/${createdId}`, { replace: true });
	}, [createdId, navigate]);

	const title = isCreate ? t('status_pages.title.create') : (page?.title ?? t('status_pages.title.update'));

	return (
		<div className="space-y-4">
			<StatusPageEditorHeader title={title} slug={page?.slug} />

			{isError ? (
				<ErrorState onRetry={() => refetch()} />
			) : !isCreate && (isLoading || !page) ? (
				<StatusPageFormSkeleton />
			) : (
				<StatusPageForm
					// A fresh form per loaded version: local state starts from what the server holds.
					key={page?.updated_at ?? 'new'}
					page={page}
					isSaving={save.isPending}
					onSubmit={save.mutateAsync}
					onSaved={({ id }) => isCreate && setCreatedId(id)}
				/>
			)}
		</div>
	);
}
