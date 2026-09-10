import {
	Sidebar,
	SidebarContent,
	SidebarFooter,
	SidebarGroup,
	SidebarGroupContent,
	SidebarGroupLabel,
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
import { useQuery } from '@tanstack/react-query';
import probeService from '@/features/probes/services/probeService.ts';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
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
import { ScrollArea } from '@/components/atoms/scroll-area.tsx';
import { Separator } from '@/components/atoms/separator.tsx';
import authServices from '@/features/auth/services/authServices.ts';
import { getInitials } from '@/lib/utils.ts';
import { useTranslation } from 'react-i18next';
import ProbeStatusEnum from '@/features/probes/enums/probe-status.enum.ts';
import { getStatusTokens } from '@/lib/status.ts';
import ThemeToggle from '@/components/molecules/theme-toggle.tsx';
import LastUpdated from '@/components/molecules/last-updated.tsx';

/** Route prefix to page title. Monitor pages title themselves with the probe name. */
const SECTION_TITLES: { prefix: string; key: string }[] = [
	{ prefix: '/dashboard', key: 'layout.sidebar.dashboard' },
	{ prefix: '/monitors/new', key: 'monitors.title.create' },
	{ prefix: '/maintenances', key: 'layout.sidebar.maintenances' },
	{ prefix: '/profile', key: 'layout.sidebar.settings' },
];

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

	const logout = async () => {
		await authServices.logout();
		navigate('/login');
	};

	const section = SECTION_TITLES.find((s) => pathname.startsWith(s.prefix));

	return (
		<SidebarProvider>
			<Sidebar>
				<SidebarHeader>
					<Link to="/dashboard" className="flex items-center gap-2 px-2 py-1.5">
						<img src="/img/logo-ui.png" alt="" className="size-6 shrink-0" />
						<span className="text-sm font-semibold tracking-tight">Uptime Kotlin</span>
					</Link>
				</SidebarHeader>

				<SidebarContent className="flex flex-col gap-4 p-2">
					<SidebarGroupContent>
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

					<SidebarGroup>
						<SidebarGroupLabel className="text-muted-foreground px-2 py-1 text-xs font-semibold">
							{t('layout.sidebar.monitors')}
						</SidebarGroupLabel>

						<ScrollArea className="h-full px-1 py-1">
							<div className="flex flex-col gap-1">
								{isLoading
									? Array.from({ length: 3 }).map((_, index) => (
											<div key={index} className="flex items-center gap-3 p-3">
												<Skeleton className="h-4 w-4 rounded" />
												<Skeleton className="h-4 flex-1" />
											</div>
										))
									: data?.map((monitor: ProbeListItem) => {
											const tokens = getStatusTokens(monitor.status);
											const isDown = monitor.status !== ProbeStatusEnum.SUCCESS;

											return (
												<Link
													key={monitor.id}
													to={`/monitors/${monitor.id}`}
													className="hover:bg-sidebar-accent group flex w-full items-center gap-3 rounded-lg p-3 text-left transition"
												>
													<span className="relative flex size-2.5 shrink-0">
														{isDown && (
															<span
																className={`motion-safe:animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${tokens.solid}`}
															/>
														)}
														<span className={`relative inline-flex size-2.5 rounded-full ${tokens.solid}`} />
													</span>

													<span className="truncate text-sm font-medium">{monitor.name}</span>
												</Link>
											);
										})}
							</div>
						</ScrollArea>
					</SidebarGroup>
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
					{section && <h1 className="text-sm font-semibold">{t(section.key)}</h1>}

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
