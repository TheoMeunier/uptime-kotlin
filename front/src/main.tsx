import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './assets/index.css';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router';
import Login from '@/pages/auth/login.tsx';
import { ProtectedRouteProvider } from '@/features/auth/contexts/protected-route-provider.tsx';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Layout from '@/components/layouts/layout.tsx';
import CreateProbe from '@/pages/probes/create-probe.tsx';
import { ShowProbe } from '@/pages/probes/show-probe.tsx';
import Profile from '@/pages/profile/profile.tsx';
import ProbesStatus from '@/pages/probes/probes-status.tsx';
import './lang/i18n.ts';
import EditProbe from '@/pages/probes/edit-probe.tsx';
import Dashboard from '@/pages/dashboard.tsx';
import SetupPage from '@/pages/setup/setup-page.tsx';
import { SetupProvider } from '@/features/setup/contexts/setup-context.tsx';
import { SetupAppProvider } from '@/features/setup/contexts/setup-app-provider.tsx';

export const queryClient = new QueryClient();

createRoot(document.getElementById('root')!).render(
	<StrictMode>
		<QueryClientProvider client={queryClient}>
			<BrowserRouter>
				<SetupProvider>
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
				</SetupProvider>
			</BrowserRouter>
		</QueryClientProvider>
	</StrictMode>
);
