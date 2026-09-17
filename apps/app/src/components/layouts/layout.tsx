import {
	Sidebar,
	SidebarContent,
	SidebarFooter,
	SidebarGroupContent,
	SidebarHeader,
	SidebarInset,
	SidebarMenu,
	SidebarMenuButton,
	SidebarMenuItem,
	SidebarProvider,
	SidebarTrigger,
} from '@/components/atoms/sidebar.tsx';
import { Button } from '@/components/atoms/button.tsx';
import { Activity, BadgeCheck, ChevronsUpDown, Home, LogOut, Plus, Wrench } from 'lucide-react';
import { Avatar, AvatarFallback } from '@/components/atoms/avatar.tsx';
import { Link, Outlet, useLocation, useNavigate } from 'react-router';
import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import probeService from '@/features/probes/services/probeService.ts';
import type { ProbeListItem } from '@/features/probes/schemas/probe-response.schema.ts';
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuGroup,
	DropdownMenuItem,
	DropdownMenuLabel,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
} from '@/components/atoms/dropdown-menu.tsx';
import { useIsMobile } from '@/hooks/use-mobile.ts';
import { Separator } from '@/components/atoms/separator.tsx';
import authServices from '@/features/auth/services/authServices.ts';
import { getInitials } from '@/lib/utils.ts';
import { useTranslation } from 'react-i18next';
import { summarizeAttention } from '@/lib/status.ts';
import { buildCrumbs, currentPageLabel } from '@/lib/breadcrumb.ts';
import useTabStatus from '@/hooks/use-tab-status.ts';
import ThemeToggle from '@/components/molecules/theme-toggle.tsx';
import LastUpdated from '@/components/molecules/last-updated.tsx';
import SidebarMonitors from '@/components/molecules/sidebar-monitors.tsx';
import PageBreadcrumb from '@/components/molecules/page-breadcrumb.tsx';

export default function Layout() {
	const { t } = useTranslation();
	const isMobile = useIsMobile();
	const user = authServices.getUser();
	const navigate = useNavigate();
	const { pathname } = useLocation();

	const { data, isLoading, dataUpdatedAt } = useQuery({
		queryKey: ['probes'],
		queryFn: async () => {
			return probeService.getProbes();
		},
		staleTime: 5 * 1000,
		refetchInterval: 60 * 1000,
		refetchOnWindowFocus: true,
		refetchIntervalInBackground: true,
	});

	const attention = useMemo(() => summarizeAttention(data?.map((probe: ProbeListItem) => probe.status)), [data]);
	const page = currentPageLabel(
		buildCrumbs(pathname, (id) => {
			const probe = data?.find((item: ProbeListItem) => item.id === id);
			return probe ? { label: probe.name } : { pending: true };
		}),
		t
	);

	useTabStatus({ ...attention, page });

	const logout = async () => {
		await authServices.logout();
		navigate('/login');
	};

	return (
		<SidebarProvider>
			<Sidebar>
				<SidebarHeader>
					<Link to="/dashboard" className="flex items-center gap-2 px-2 py-1.5">
						<img src="/img/logo-ui.png" alt="" className="size-6 shrink-0" />
						<span className="text-sm font-semibold tracking-tight">Uptime Kotlin</span>
					</Link>
				</SidebarHeader>

				<SidebarContent className="flex min-h-0 flex-col gap-4 overflow-hidden p-2">
					<SidebarGroupContent className="shrink-0">
						<Button asChild className="flex w-full items-center gap-2 p-3 font-medium">
							<Link to={'/monitors/new'}>
								<Plus size={18} /> {t('layout.sidebar.new_monitor')}
							</Link>
						</Button>
						<SidebarMenuItem className="mt-6 space-y-2">
							<SidebarMenuButton asChild isActive={pathname.startsWith('/dashboard')}>
								<Link to={'/dashboard'}>
									<Home size={18} /> {t('layout.sidebar.dashboard')}
								</Link>
							</SidebarMenuButton>
							<SidebarMenuButton asChild isActive={pathname.startsWith('/maintenances')}>
								<Link to={'/maintenances'}>
									<Wrench size={18} /> {t('layout.sidebar.maintenances')}
								</Link>
							</SidebarMenuButton>
							<SidebarMenuButton asChild isActive={pathname.startsWith('/status')}>
								<Link to={'/status'}>
									<Activity size={18} /> {t('layout.sidebar.status_page')}
								</Link>
							</SidebarMenuButton>
						</SidebarMenuItem>
					</SidebarGroupContent>

					<SidebarMonitors probes={data} isLoading={isLoading} />
				</SidebarContent>

				<SidebarFooter>
					<SidebarMenu>
						<SidebarMenuItem>
							<DropdownMenu>
								<DropdownMenuTrigger asChild>
									<SidebarMenuButton
										size="lg"
										className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
									>
										<Avatar className="h-8 w-8 rounded-lg">
											<AvatarFallback className="rounded-lg">{getInitials(user.username)}</AvatarFallback>
										</Avatar>
										<div className="grid flex-1 text-left text-sm leading-tight">
											<span className="truncate font-medium">{user.username}</span>
											<span className="text-muted-foreground truncate text-xs">{user.email}</span>
										</div>
										<ChevronsUpDown className="ml-auto size-4" />
									</SidebarMenuButton>
								</DropdownMenuTrigger>
								<DropdownMenuContent
									className="w-(--radix-dropdown-menu-trigger-width) min-w-56 rounded-lg"
									side={isMobile ? 'bottom' : 'right'}
									align="end"
									sideOffset={4}
								>
									<DropdownMenuLabel className="p-0 font-normal">
										<div className="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
											<Avatar className="h-8 w-8 rounded-lg">
												<AvatarFallback className="rounded-lg">{getInitials(user.username)}</AvatarFallback>
											</Avatar>
											<div className="grid flex-1 text-left text-sm leading-tight">
												<span className="truncate font-medium">{user.username}</span>
												<span className="text-muted-foreground truncate text-xs">{user.email}</span>
											</div>
										</div>
									</DropdownMenuLabel>
									<DropdownMenuSeparator />
									<DropdownMenuGroup>
										<Link to={'/profile'}>
											<DropdownMenuItem>
												<BadgeCheck />
												{t('layout.sidebar.settings')}
											</DropdownMenuItem>
										</Link>

										<DropdownMenuItem onClick={logout}>
											<LogOut />
											{t('layout.sidebar.logout')}
										</DropdownMenuItem>
									</DropdownMenuGroup>
								</DropdownMenuContent>
							</DropdownMenu>
						</SidebarMenuItem>
					</SidebarMenu>
				</SidebarFooter>
			</Sidebar>

			<SidebarInset>
				<header className="flex h-14 shrink-0 items-center gap-2 border-b px-4">
					<SidebarTrigger className="-ml-1" />
					<Separator orientation="vertical" className="mr-1 data-[orientation=vertical]:h-4" />
					<PageBreadcrumb />

					<div className="ml-auto flex items-center gap-2">
						<LastUpdated at={dataUpdatedAt} />
						<ThemeToggle />
					</div>
				</header>
				<main className="mx-auto w-full max-w-7xl flex-1 px-4 py-8">
					<Outlet />
				</main>
			</SidebarInset>
		</SidebarProvider>
	);
}
