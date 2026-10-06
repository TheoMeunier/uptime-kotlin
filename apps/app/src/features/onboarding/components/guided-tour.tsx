import { useEffect, useLayoutEffect, useMemo, useState } from 'react';
import * as PopoverPrimitive from '@radix-ui/react-popover';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router';
import { ArrowLeft, ArrowRight, Plus, X } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';
import { cn } from '@/lib/utils.ts';
import { TOUR_STEPS } from '@/features/onboarding/tour-steps.ts';

const SPOTLIGHT_PADDING = 6;

interface GuidedTourProps {
	onClose: () => void;
}

interface TargetState {
	target: string | undefined;
	rect: DOMRect | null;
}

function isOnScreen(rect: DOMRect): boolean {
	return (
		rect.width > 0 &&
		rect.height > 0 &&
		rect.right > 0 &&
		rect.bottom > 0 &&
		rect.left < window.innerWidth &&
		rect.top < window.innerHeight
	);
}

function useTargetRect(target: string | undefined): DOMRect | null {
	const [state, setState] = useState<TargetState>({ target: undefined, rect: null });

	useLayoutEffect(() => {
		if (!target) return;

		const element = document.querySelector<HTMLElement>(`[data-tour="${target}"]`);
		let frame = 0;

		const measure = () => {
			const rect = element?.getBoundingClientRect() ?? null;
			setState({ target, rect: rect && isOnScreen(rect) ? rect : null });
		};
		const schedule = () => {
			cancelAnimationFrame(frame);
			frame = requestAnimationFrame(measure);
		};

		element?.scrollIntoView({ block: 'nearest' });
		measure();

		const observer = element ? new ResizeObserver(schedule) : null;
		if (element) observer?.observe(element);
		window.addEventListener('resize', schedule);
		window.addEventListener('scroll', schedule, true);

		return () => {
			cancelAnimationFrame(frame);
			observer?.disconnect();
			window.removeEventListener('resize', schedule);
			window.removeEventListener('scroll', schedule, true);
		};
	}, [target]);

	return target && state.target === target ? state.rect : null;
}

function centeredAnchor(): DOMRect {
	return new DOMRect(window.innerWidth / 2, window.innerHeight * 0.3, 0, 0);
}

export default function GuidedTour({ onClose }: GuidedTourProps) {
	const { t } = useTranslation();
	const navigate = useNavigate();
	const [index, setIndex] = useState(0);

	const step = TOUR_STEPS[index];
	const total = TOUR_STEPS.length;
	const isFirst = index === 0;
	const isLast = index === total - 1;

	const rect = useTargetRect(step.target);

	const anchor = useMemo(() => ({ current: { getBoundingClientRect: () => rect ?? centeredAnchor() } }), [rect, index]);

	const next = () => (isLast ? onClose() : setIndex((value) => value + 1));
	const previous = () => setIndex((value) => Math.max(0, value - 1));

	useEffect(() => {
		const onKeyDown = (event: KeyboardEvent) => {
			if (event.key === 'ArrowRight') setIndex((value) => Math.min(total - 1, value + 1));
			if (event.key === 'ArrowLeft') setIndex((value) => Math.max(0, value - 1));
		};

		window.addEventListener('keydown', onKeyDown);
		return () => window.removeEventListener('keydown', onKeyDown);
	}, [total]);

	const createFirstMonitor = () => {
		onClose();
		navigate('/monitors/new');
	};

	return (
		<PopoverPrimitive.Root open modal>
			<PopoverPrimitive.Portal>
				{rect ? (
					<div
						aria-hidden
						className="pointer-events-none fixed z-50 rounded-lg ring-2 ring-primary transition-all duration-300 ease-out motion-reduce:transition-none"
						style={{
							top: rect.top - SPOTLIGHT_PADDING,
							left: rect.left - SPOTLIGHT_PADDING,
							width: rect.width + SPOTLIGHT_PADDING * 2,
							height: rect.height + SPOTLIGHT_PADDING * 2,
							boxShadow: '0 0 0 9999px rgb(0 0 0 / 0.55)',
						}}
					/>
				) : (
					<div aria-hidden className="fixed inset-0 z-50 bg-black/55" />
				)}
			</PopoverPrimitive.Portal>

			<PopoverPrimitive.Anchor virtualRef={anchor} />

			<PopoverPrimitive.Portal>
				<PopoverPrimitive.Content
					role="dialog"
					aria-modal="true"
					aria-labelledby="guided-tour-title"
					aria-describedby="guided-tour-description"
					side={rect ? step.side : 'bottom'}
					align={rect ? 'start' : 'center'}
					sideOffset={rect ? SPOTLIGHT_PADDING + 10 : 0}
					collisionPadding={16}
					onInteractOutside={(event) => event.preventDefault()}
					onEscapeKeyDown={onClose}
					className={cn(
						'bg-popover text-popover-foreground z-50 w-[min(24rem,calc(100vw-2rem))] rounded-lg border p-5 shadow-lg outline-hidden',
						'data-[state=open]:animate-in data-[state=open]:fade-in-0 data-[state=open]:zoom-in-95'
					)}
				>
					<div className="mb-3 flex items-start justify-between gap-4">
						<p className="text-muted-foreground text-xs font-medium tabular-nums">
							{t('onboarding.progress', { current: index + 1, total })}
						</p>
						<button
							type="button"
							onClick={onClose}
							aria-label={t('onboarding.actions.skip')}
							className="text-muted-foreground hover:text-foreground focus-visible:ring-ring -mt-1 -mr-1 rounded-sm p-1 transition-colors focus-visible:ring-2 focus-visible:outline-none"
						>
							<X className="size-4" />
						</button>
					</div>

					<h2 id="guided-tour-title" className="text-base font-semibold tracking-tight">
						{t(`onboarding.steps.${step.id}.title`)}
					</h2>
					<p id="guided-tour-description" className="text-muted-foreground mt-2 text-sm leading-relaxed">
						{t(`onboarding.steps.${step.id}.description`)}
					</p>

					<div className="mt-4 flex gap-1.5" aria-hidden>
						{TOUR_STEPS.map((item, position) => (
							<span
								key={item.id}
								className={cn(
									'h-1 flex-1 rounded-full transition-colors',
									position <= index ? 'bg-primary' : 'bg-muted'
								)}
							/>
						))}
					</div>

					<div className="mt-5 flex items-center justify-between gap-2">
						{isFirst ? (
							<Button variant="ghost" size="sm" onClick={onClose}>
								{t('onboarding.actions.skip')}
							</Button>
						) : isLast ? (
							<Button variant="ghost" size="icon-sm" onClick={previous} aria-label={t('onboarding.actions.previous')}>
								<ArrowLeft className="size-4" />
							</Button>
						) : (
							<Button variant="ghost" size="sm" onClick={previous}>
								<ArrowLeft className="size-4" />
								{t('onboarding.actions.previous')}
							</Button>
						)}

						<div className="flex items-center gap-2">
							{isLast && (
								<Button variant="outline" size="sm" onClick={onClose}>
									{t('onboarding.actions.finish')}
								</Button>
							)}
							{isLast ? (
								<Button size="sm" onClick={createFirstMonitor} autoFocus>
									<Plus className="size-4" />
									{t('onboarding.actions.create_monitor')}
								</Button>
							) : (
								<Button size="sm" onClick={next} autoFocus>
									{isFirst ? t('onboarding.actions.start') : t('onboarding.actions.next')}
									<ArrowRight className="size-4" />
								</Button>
							)}
						</div>
					</div>
				</PopoverPrimitive.Content>
			</PopoverPrimitive.Portal>
		</PopoverPrimitive.Root>
	);
}
