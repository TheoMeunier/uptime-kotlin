import { StrictMode, Suspense } from 'react';
import { createRoot } from 'react-dom/client';
import './assets/index.css';
import { createBrowserRouter, createRoutesFromElements, Navigate, Route, RouterProvider } from 'react-router';
import { ProtectedRouteProvider } from '@/features/auth/contexts/protected-route-provider.tsx';
import { MutationCache, QueryCache, QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Layout from '@/components/layouts/layout.tsx';
import './lang/i18n.ts';
import { SetupProvider } from '@/features/setup/contexts/setup-context.tsx';
import { SetupAppProvider } from '@/features/setup/contexts/setup-app-provider.tsx';
import LoaderPage from '@/features/setup/components/loader-page.tsx';
import { toast } from 'sonner';
import { getApiErrorMessage } from '@/api/api-error.ts';
import { Toaster } from '@/components/atoms/sonner.tsx';
import { ThemeProvider } from 'next-themes';
import AppErrorBoundary from '@/components/molecules/app-error-boundary.tsx';
import RouteErrorBoundary from '@/components/molecules/route-error-boundary.tsx';
import { reloadOnce } from '@/lib/chunk-error.ts';
import {
	CreateProbe,
	Dashboard,
	EditProbe,
	Login,
	Maintenances,
	ProbesStatus,
	Profile,
	SetupPage,
	ShowProbe,
} from '@/pages/lazy-pages.ts';

window.addEventListener('vite:preloadError', (event) => {
	if (reloadOnce()) event.preventDefault();
});

const DEFAULT_STALE_TIME_MS = 10_000;

const queryClient = new QueryClient({
	defaultOptions: {
		queries: {
			staleTime: DEFAULT_STALE_TIME_MS,
		},
	},
	queryCache: new QueryCache({
		onError: (error, query) => {
			if (query.meta?.silentError === true) return;
			toast.error(getApiErrorMessage(error));
		},
	}),
	mutationCache: new MutationCache({
		onError: (error) => {
			toast.error(getApiErrorMessage(error));
		},
	}),
});

const router = createBrowserRouter(
	createRoutesFromElements(
		<>
			<Route path="/" element={<SetupAppProvider />} errorElement={<RouteErrorBoundary fullscreen />}>
				<Route path="/" element={<ProtectedRouteProvider />}>
					<Route path="/" element={<Layout />}>
						<Route errorElement={<RouteErrorBoundary />}>
							<Route path="/dashboard" element={<Dashboard />} />

							<Route path="monitors/new" element={<CreateProbe />} />
							<Route path="monitors/:probeId/edit" element={<EditProbe />} />
							<Route path="monitors/:probeId" element={<ShowProbe />} />

							<Route path="maintenances" element={<Maintenances />} />

							<Route path="profile" element={<Profile />} />
						</Route>
					</Route>
				</Route>

				<Route path="/status" element={<ProbesStatus />} />
				<Route path="/login" element={<Login />} />

				<Route path="*" element={<Navigate to="/dashboard" replace />} />
			</Route>

			<Route path="/setup" element={<SetupPage />} errorElement={<RouteErrorBoundary fullscreen />} />
		</>
	)
);

createRoot(document.getElementById('root')!).render(
	<StrictMode>
		<ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
			<AppErrorBoundary>
				<QueryClientProvider client={queryClient}>
					<SetupProvider>
						<Suspense fallback={<LoaderPage />}>
							<RouterProvider router={router} />
						</Suspense>
						<Toaster />
					</SetupProvider>
				</QueryClientProvider>
			</AppErrorBoundary>
		</ThemeProvider>
	</StrictMode>
);
