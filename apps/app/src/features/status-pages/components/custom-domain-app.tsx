import { Suspense, useState } from 'react';
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router';
import RouteErrorBoundary from '@/components/molecules/route-error-boundary.tsx';
import StatusBoardFallback from '@/features/status-pages/components/board/status-board-fallback.tsx';
import { PublicStatusPage } from '@/pages/lazy-pages.ts';

export default function CustomDomainApp({ slug }: { slug: string }) {
	const [router] = useState(() =>
		createBrowserRouter([
			{
				path: '/',
				errorElement: <RouteErrorBoundary fullscreen />,
				element: (
					<Suspense fallback={<StatusBoardFallback />}>
						<PublicStatusPage slug={slug} standalone />
					</Suspense>
				),
			},
			{ path: '*', element: <Navigate to="/" replace /> },
		])
	);

	return <RouterProvider router={router} />;
}
