import { useReducer } from 'react';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import useRestoreBackup from '@/features/backup/hooks/useRestoreBackup.ts';
import { type RestoreReport, type ConflictStrategy, countOutcomes } from '@/features/backup/schemas/backup.schema.ts';

type RestoreFlowState = {
	source: File | null;
	content: unknown;
	fileError: boolean;
	onConflict: ConflictStrategy;
	report: RestoreReport | null;
};

type RestoreFlowEvent =
	| { type: 'file_loaded'; source: File; content: unknown }
	| { type: 'file_invalid'; source: File }
	| { type: 'file_cleared' }
	| { type: 'conflict_changed'; onConflict: ConflictStrategy }
	| { type: 'reported'; report: RestoreReport };

const INITIAL_STATE: RestoreFlowState = {
	source: null,
	content: null,
	fileError: false,
	onConflict: 'skip',
	report: null,
};

function reduce(state: RestoreFlowState, event: RestoreFlowEvent): RestoreFlowState {
	switch (event.type) {
		case 'file_loaded':
			return { ...state, source: event.source, content: event.content, fileError: false, report: null };
		case 'file_invalid':
			return { ...state, source: event.source, content: null, fileError: true, report: null };
		case 'file_cleared':
			return { ...state, source: null, content: null, fileError: false, report: null };
		case 'conflict_changed':
			return { ...state, onConflict: event.onConflict, report: null };
		case 'reported':
			return event.report.dry_run
				? { ...state, report: event.report }
				: { ...state, report: event.report, source: null, content: null };
	}
}

export default function useRestoreFlow() {
	const { t } = useTranslation();
	const [state, dispatch] = useReducer(reduce, INITIAL_STATE);
	const { restoreBackup, isRestoring } = useRestoreBackup();

	const counts = state.report ? countOutcomes(state.report) : null;
	const hasFile = state.content !== null;

	const selectFile = async (source: File | null) => {
		if (!source) return dispatch({ type: 'file_cleared' });

		try {
			dispatch({ type: 'file_loaded', source, content: JSON.parse(await source.text()) });
		} catch {
			dispatch({ type: 'file_invalid', source });
		}
	};

	const run = (dryRun: boolean) =>
		restoreBackup(
			{ file: state.content, dryRun, onConflict: state.onConflict },
			{
				onSuccess: (report) => {
					dispatch({ type: 'reported', report });
					if (dryRun) return;
					toast.success(t('backup.restore.done', { count: countOutcomes(report).create }));
				},
			}
		);

	return {
		...state,
		counts,
		isRestoring,
		canAnalyze: hasFile && !isRestoring,
		canConfirm: hasFile && state.report?.dry_run === true && (counts?.create ?? 0) > 0,
		selectFile,
		setOnConflict: (onConflict: ConflictStrategy) => dispatch({ type: 'conflict_changed', onConflict }),
		analyze: () => run(true),
		confirm: () => run(false),
	};
}
