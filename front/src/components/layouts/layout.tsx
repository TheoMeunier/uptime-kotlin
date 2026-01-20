import {
	Sidebar,
	SidebarContent,
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
import { Activity, BadgeCheck, ChevronsUpDown, Home, LogOut, Plus } from 'lucide-react';
import { Avatar, AvatarFallback } from '@/components/atoms/avatar.tsx';
import { Link, Outlet, useNavigate } from 'react-router';
import { useQuery } from '@tanstack/react-query';
import probeService from '@/features/probes/services/probeService.ts';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import type { ProbeListItem } from '@/features/probes/schemas/probe-response.schema.ts';
import { Toaster } from '@/components/atoms/sonner.tsx';
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

export default function Layout() {
	const isMobile = useIsMobile();
	const user = authServices.getUser();
	const navigate = useNavigate();

	const { data, isLoading } = useQuery({
		queryKey: ['probes'],
		queryFn: async () => {
			return probeService.getProbes();
		},
		staleTime: 5 * 60 * 1000, // 5 minutes
	});

	const logout = () => {
		authServices.logout();
		navigate('/login');
	};

	return (
		<SidebarProvider>
			<Sidebar>
				<SidebarHeader>
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
											<span className="truncate text-xs">{user.email}</span>
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
												<span className="truncate text-xs">{user.email}</span>
											</div>
										</div>
									</DropdownMenuLabel>
									<DropdownMenuSeparator />
									<DropdownMenuGroup>
										<Link to={'/profile'}>
											<DropdownMenuItem>
												<BadgeCheck />
												Account
											</DropdownMenuItem>
										</Link>

										<DropdownMenuItem onClick={logout}>
											<LogOut />
											Log out
										</DropdownMenuItem>
									</DropdownMenuGroup>
								</DropdownMenuContent>
							</DropdownMenu>
						</SidebarMenuItem>
					</SidebarMenu>
				</SidebarHeader>

				<SidebarContent className="flex flex-col gap-4 p-2">
					<SidebarGroupContent>
						<Button asChild className="w-full flex items-center gap-2 shadow-sm p-3 font-medium">
							<Link to={'/monitors/new'}>
								<Plus size={18} /> New monitor
							</Link>
						</Button>
						<SidebarMenuItem className="mt-6 space-y-2">
							<SidebarMenuButton asChild>
								<Link to={'/dashboard'}>
									<Home size={18} /> Dashboard
								</Link>
							</SidebarMenuButton>
							<SidebarMenuButton asChild>
								<Link to={'/status'}>
									<Activity size={18} /> Status Page
								</Link>
							</SidebarMenuButton>
						</SidebarMenuItem>
					</SidebarGroupContent>

					<SidebarGroup>
						<SidebarGroupLabel className="text-xs font-semibold text-muted-foreground px-2 py-1">
							Monitors
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
									: data?.map((monitor: ProbeListItem) => (
											<Link
												key={monitor.id}
												to={`/monitors/${monitor.id}`}
												className="w-full flex items-center gap-3 p-3 rounded-lg hover:bg-gray-100 transition text-left group"
											>
												{monitor.status === 'SUCCESS' ? (
													<span className="relative flex size-3">
														<span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-green-400 opacity-75" />
														<span className="relative inline-flex size-3 rounded-full bg-green-500" />
													</span>
												) : (
													<span className="relative flex size-3">
														<span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-red-400 opacity-75" />
														<span className="relative inline-flex size-3 rounded-full bg-red-500" />
													</span>
												)}

												<span className="font-medium text-sm truncate">{monitor.name}</span>
											</Link>
										))}
							</div>
						</ScrollArea>
					</SidebarGroup>
				</SidebarContent>
			</Sidebar>
			<SidebarInset>
				<header className="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12">
					<div className="flex items-center gap-2 px-4">
						<SidebarTrigger className="-ml-1" />
						<Separator orientation="vertical" className="mr-2 data-[orientation=vertical]:h-4" />
					</div>
				</header>
				<main className="container mx-auto flex-1 px-4 py-8">
					<Outlet />
					<Toaster />
				</main>
			</SidebarInset>
		</SidebarProvider>
	);
}
