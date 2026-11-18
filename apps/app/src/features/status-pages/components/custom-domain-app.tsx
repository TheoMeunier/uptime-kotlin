import { Suspense, useState } from 'react';
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router';
import RouteErrorBoundary from '@/components/molecules/route-error-boundary.tsx';
import StatusBoardFallback from '@/features/status-pages/components/board/status-board-fallback.tsx';
import { PublicStatusPage } from '@/pages/lazy-pages.ts';

/**
 * The whole app on a custom domain: the status page at `/`, every other path sends back to it. No setup check, no
 * login, no admin: those stay on the instance's own domain.
 */
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
