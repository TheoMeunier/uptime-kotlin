import { useTranslation } from 'react-i18next';
import { Activity } from 'lucide-react';
import LanguageToggle from '@/components/molecules/language-toggle.tsx';
import ViewModeToggle from '@/components/molecules/view-mode-toggle.tsx';
import StatusSummary from '@/features/status-pages/components/board/status-summary.tsx';
import type { StatusItem } from '@/features/status-pages/lib/status-item.ts';
import type { StatusLayout } from '@/features/status-pages/lib/status-layout.ts';

interface StatusBoardHeaderProps {
	title: string;
	subtitle?: string | null;
	items: StatusItem[];
	layout: StatusLayout;
	onLayoutChange: (layout: StatusLayout) => void;
}

export default function StatusBoardHeader({ title, subtitle, items, layout, onLayoutChange }: StatusBoardHeaderProps) {
	const { t } = useTranslation();

	return (
		<div className="bg-card border-b">
			<div className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
				<div className="mb-3 flex items-center gap-3">
					<div className="bg-primary/10 shrink-0 rounded-lg p-2">
						<Activity className="text-primary h-6 w-6" />
					</div>
					<div className="min-w-0">
						<h1 className="text-foreground text-2xl font-semibold tracking-tight sm:text-3xl">{title}</h1>
						{subtitle && <p className="text-muted-foreground mt-1 text-sm sm:text-base">{subtitle}</p>}
					</div>

					<div className="ml-auto flex items-center gap-1 self-start">
						<ViewModeToggle
							value={layout}
							onChange={onLayoutChange}
							aria-label={t('pages.status_page.layout.label')}
							className="hidden md:flex"
						/>
						<LanguageToggle />
					</div>
				</div>

				<StatusSummary items={items} />
			</div>
		</div>
	);
}
