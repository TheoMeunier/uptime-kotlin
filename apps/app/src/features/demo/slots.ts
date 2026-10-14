import { lazy } from 'react';

export const DemoBanner = __DEMO_BUILD__ ? lazy(() => import('@/features/demo/components/demo-banner.tsx')) : null;

export const DemoLoginHint = __DEMO_BUILD__
	? lazy(() => import('@/features/demo/components/demo-login-hint.tsx'))
	: null;
