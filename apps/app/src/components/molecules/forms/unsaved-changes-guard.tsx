import { useCallback, useEffect } from 'react';
import { type BlockerFunction, useBlocker } from 'react-router';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/atoms/button.tsx';
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
} from '@/components/atoms/dialog.tsx';

export default function UnsavedChangesGuard({ when }: { when: boolean }) {
	const { t } = useTranslation();

	const shouldBlock = useCallback<BlockerFunction>(
		({ currentLocation, nextLocation }) =>
			when && (currentLocation.pathname !== nextLocation.pathname || currentLocation.search !== nextLocation.search),
		[when]
	);
	const blocker = useBlocker(shouldBlock);

	useEffect(() => {
		if (!when) return;

		const onBeforeUnload = (event: BeforeUnloadEvent) => {
			event.preventDefault();
			event.returnValue = '';
		};
		window.addEventListener('beforeunload', onBeforeUnload);
		return () => window.removeEventListener('beforeunload', onBeforeUnload);
	}, [when]);

	useEffect(() => {
		if (blocker.state === 'blocked' && !when) blocker.proceed();
	}, [blocker, when]);

	return (
		<Dialog
			open={blocker.state === 'blocked'}
			onOpenChange={(open) => {
				if (!open && blocker.state === 'blocked') blocker.reset();
			}}
		>
			<DialogContent className="sm:max-w-md">
				<DialogHeader>
					<DialogTitle>{t('unsaved_changes.title')}</DialogTitle>
					<DialogDescription>{t('unsaved_changes.description')}</DialogDescription>
				</DialogHeader>

				<DialogFooter>
					<Button type="button" variant="outline" onClick={() => blocker.reset?.()} autoFocus>
						{t('unsaved_changes.stay')}
					</Button>
					<Button type="button" variant="destructive" onClick={() => blocker.proceed?.()}>
						{t('unsaved_changes.leave')}
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}
