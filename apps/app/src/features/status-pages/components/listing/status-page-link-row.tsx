import { ExternalLink } from 'lucide-react';

interface StatusPageLinkRowProps {
	icon: React.ReactNode;
	title: string;
	href: string;
	meta: string;
	actions?: React.ReactNode;
}

export default function StatusPageLinkRow({ icon, title, href, meta, actions }: StatusPageLinkRowProps) {
	return (
		<div className="flex items-center gap-3 px-4 py-4 sm:gap-4 sm:px-6">
			<span className="bg-primary/10 text-primary flex size-9 shrink-0 items-center justify-center rounded-lg">
				{icon}
			</span>

			<div className="min-w-0 flex-1">
				<p className="truncate font-medium">{title}</p>
				<p className="text-muted-foreground flex min-w-0 flex-wrap items-center gap-x-2 text-sm">
					<a
						href={href}
						target="_blank"
						rel="noreferrer"
						className="hover:text-foreground inline-flex min-w-0 items-center gap-1 underline-offset-4 hover:underline"
					>
						<span className="truncate">{href}</span>
						<ExternalLink className="size-3 shrink-0" />
					</a>
					<span aria-hidden>·</span>
					<span className="truncate">{meta}</span>
				</p>
			</div>

			{actions && <div className="flex shrink-0 items-center gap-2">{actions}</div>}
		</div>
	);
}
