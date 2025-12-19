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
} from "@/components/atoms/sidebar.tsx";
import { Button } from "@/components/atoms/button.tsx";
import { ChevronsUpDown, Plus } from "lucide-react";
import { ScrollArea } from "@radix-ui/react-scroll-area";
import { Avatar } from "@/components/atoms/avatar.tsx";
import { AvatarFallback, AvatarImage } from "@radix-ui/react-avatar";
import { Link, Outlet } from "react-router";
import { Separator } from "@radix-ui/react-separator";
import { useQuery } from "@tanstack/react-query";
import probeService from "@/features/probes/services/probeService.ts";
import { Skeleton } from "@/components/atoms/skeleton.tsx";
import type { ProbeListItem } from "@/features/probes/schemas/probe-response.schema.ts";
import { Toaster } from "@/components/atoms/sonner.tsx";

export default function Layout() {
  const { data, isLoading } = useQuery({
    queryKey: ["probes"],
    queryFn: async () => {
      return probeService.getProbes();
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  return (
    <SidebarProvider>
      <Sidebar>
        <SidebarHeader>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton
                size="lg"
                className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground rounded-xl hover:bg-gray-100 transition"
              >
                <Avatar className="h-9 w-9 rounded-lg">
                  <AvatarImage src="/im.png" alt="Théo Meunier avatar" />
                  <AvatarFallback className="rounded-lg">TM</AvatarFallback>
                </Avatar>
                <div className="grid flex-1 text-left text-sm leading-tight">
                  <span className="truncate font-semibold">Théo Meunier</span>
                  <span className="truncate text-xs text-muted-foreground">
                    contact@theomeunier.fr
                  </span>
                </div>
                <ChevronsUpDown className="ml-auto size-4 opacity-70" />
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarHeader>

        <SidebarContent className="flex flex-col gap-4 p-2">
          <SidebarGroupContent>
            <Button
              asChild
              className="w-full flex items-center gap-2 shadow-sm p-3 font-medium"
            >
              <Link to={"/dashboard/monitors/new"}>
                <Plus size={18} /> New monitor
              </Link>
            </Button>
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
                        to={`/dashboard/monitors/${monitor.id}`}
                        className="w-full flex items-center gap-3 p-3 rounded-lg hover:bg-gray-100 transition text-left group"
                      >
                        {monitor.status === "SUCCESS" ? (
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

                        <span className="font-medium text-sm truncate">
                          {monitor.name}
                        </span>
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
            <Separator
              orientation="vertical"
              className="mr-2 data-[orientation=vertical]:h-4"
            />
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
