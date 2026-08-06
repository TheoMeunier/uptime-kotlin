import { lazy, StrictMode, Suspense } from 'react';
import { createRoot } from 'react-dom/client';
import './assets/index.css';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router';
import { ProtectedRouteProvider } from '@/features/auth/contexts/protected-route-provider.tsx';
import { MutationCache, QueryCache, QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Layout from '@/components/layouts/layout.tsx';
import './lang/i18n.ts';
import { SetupProvider } from '@/features/setup/contexts/setup-context.tsx';
import { SetupAppProvider } from '@/features/setup/contexts/setup-app-provider.tsx';
import { toast } from 'sonner';
import { getApiErrorMessage } from '@/api/api-error.ts';
import { Toaster } from '@/components/atoms/sonner.tsx';

const Dashboard = lazy(() => import('@/pages/dashboard.tsx'));
const Login = lazy(() => import('@/pages/auth/login.tsx'));
const CreateProbe = lazy(() => import('@/pages/probes/create-probe.tsx'));
const EditProbe = lazy(() => import('@/pages/probes/edit-probe.tsx'));
const ShowProbe = lazy(() => import('@/pages/probes/show-probe.tsx').then((module) => ({ default: module.ShowProbe })));
const Profile = lazy(() => import('@/pages/profile/profile.tsx'));
const ProbesStatus = lazy(() => import('@/pages/probes/probes-status.tsx'));
const SetupPage = lazy(() => import('@/pages/setup/setup-page.tsx'));

export const queryClient = new QueryClient({
	queryCache: new QueryCache({
		onError: (error) => {
			toast.error(getApiErrorMessage(error));
		},
	}),
	mutationCache: new MutationCache({
		onError: (error) => {
			toast.error(getApiErrorMessage(error));
		},
	}),
});

createRoot(document.getElementById('root')!).render(
	<StrictMode>
		<QueryClientProvider client={queryClient}>
			<BrowserRouter>
				<SetupProvider>
					<Suspense fallback={<div className="p-6 text-sm text-muted-foreground">Loading…</div>}>
						<Routes>
							<Route path="/" element={<SetupAppProvider />}>
								<Route path="/" element={<ProtectedRouteProvider />}>
									<Route path="/" element={<Layout />}>
										<Route path="/dashboard" element={<Dashboard />} />

										<Route path="monitors/new" element={<CreateProbe />} />
										<Route path="monitors/:probeId/edit" element={<EditProbe />} />
										<Route path="monitors/:probeId" element={<ShowProbe />} />

										<Route path="profile" element={<Profile />} />
									</Route>
								</Route>

								<Route path="/status" element={<ProbesStatus />} />
								<Route path="/login" element={<Login />} />

								<Route path="*" element={<Navigate to="/dashboard" replace />} />
							</Route>

							<Route path="/setup" element={<SetupPage />} />
						</Routes>
					</Suspense>
					<Toaster />
				</SetupProvider>
			</BrowserRouter>
		</QueryClientProvider>
	</StrictMode>
);
