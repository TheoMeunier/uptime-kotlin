import { Link } from 'react-router';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, ExternalLink } from 'lucide-react';
import { Button } from '@/components/atoms/button.tsx';

interface StatusPageEditorHeaderProps {
	title: string;
	/* Saved address of the page; the "view" button only appears once it exists. */
	slug?: string;
}

export default function StatusPageEditorHeader({ title, slug }: StatusPageEditorHeaderProps) {
	const { t } = useTranslation();

	return (
		<>
			<Button variant="ghost" size="sm" asChild className="text-muted-foreground -ml-2">
				<Link to="/status-pages">
					<ArrowLeft className="size-4" />
					{t('status_pages.actions.back')}
				</Link>
			</Button>

			<section className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
				<div className="min-w-0">
					<h1 className="truncate text-2xl font-semibold tracking-tight">{title}</h1>
					<p className="text-muted-foreground mt-1 text-sm">{t('status_pages.description.editor')}</p>
				</div>

				{slug && (
					<Button variant="outline" asChild className="shrink-0">
						<a href={`/status/${slug}`} target="_blank" rel="noreferrer">
							<ExternalLink className="size-4" />
							{t('status_pages.actions.view')}
						</a>
					</Button>
				)}
			</section>
		</>
	);
}
