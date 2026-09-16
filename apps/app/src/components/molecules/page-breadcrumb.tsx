import { Fragment } from 'react';
import { Link, useLocation } from 'react-router';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';

import {
	Breadcrumb,
	BreadcrumbItem,
	BreadcrumbLink,
	BreadcrumbList,
	BreadcrumbPage,
	BreadcrumbSeparator,
} from '@/components/atoms/breadcrumb.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import probeService from '@/features/probes/services/probeService.ts';
import { buildCrumbs } from '@/lib/breadcrumb.ts';

export default function PageBreadcrumb() {
	const { t } = useTranslation();
	const { pathname } = useLocation();

	const { data: probes, isLoading } = useQuery({
		queryKey: ['probes'],
		queryFn: () => probeService.getProbes(),
		staleTime: 5 * 1000,
		enabled: pathname.startsWith('/monitors/'),
	});

	const crumbs = buildCrumbs(pathname, (id) => {
		const probe = probes?.find((item) => item.id === id);
		if (probe) return { label: probe.name };

		return isLoading ? { pending: true } : { label: t('layout.breadcrumb.monitor') };
	});

	if (crumbs.length === 0) return null;

	return (
		<Breadcrumb>
			<BreadcrumbList className="sm:gap-1.5">
				{crumbs.map((crumb, index) => {
					const isLast = index === crumbs.length - 1;
					const label = crumb.key ? t(crumb.key) : crumb.label;

					return (
						<Fragment key={`${crumb.key ?? crumb.label ?? 'crumb'}-${index}`}>
							<BreadcrumbItem className="max-w-[10rem] sm:max-w-xs">
								{crumb.pending ? (
									<Skeleton className="h-4 w-24" />
								) : isLast || !crumb.to ? (
									<BreadcrumbPage className="truncate font-medium">{label}</BreadcrumbPage>
								) : (
									<BreadcrumbLink asChild className="truncate">
										<Link to={crumb.to}>{label}</Link>
									</BreadcrumbLink>
								)}
							</BreadcrumbItem>
							{!isLast && <BreadcrumbSeparator />}
						</Fragment>
					);
				})}
			</BreadcrumbList>
		</Breadcrumb>
	);
}
