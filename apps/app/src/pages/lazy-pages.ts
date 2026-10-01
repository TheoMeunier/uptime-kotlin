import { lazy } from 'react';

export const Dashboard = lazy(() => import('@/pages/dashboard.tsx'));
export const Login = lazy(() => import('@/pages/auth/login.tsx'));
export const CreateProbe = lazy(() => import('@/pages/probes/create-probe.tsx'));
export const EditProbe = lazy(() => import('@/pages/probes/edit-probe.tsx'));
export const ShowProbe = lazy(() =>
	import('@/pages/probes/show-probe.tsx').then((module) => ({ default: module.ShowProbe }))
);
export const Profile = lazy(() => import('@/pages/profile/profile.tsx'));
export const ProbesStatus = lazy(() => import('@/pages/probes/probes-status.tsx'));
export const Maintenances = lazy(() => import('@/pages/maintenances/maintenances.tsx'));
export const SetupPage = lazy(() => import('@/pages/setup/setup-page.tsx'));
