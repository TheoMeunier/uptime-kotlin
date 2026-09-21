import { useEffect, useState } from 'react';
import { isRouteErrorResponse, useRouteError } from 'react-router';
import ErrorScreen from '@/components/molecules/error-screen.tsx';
import { isChunkLoadError, reloadOnce } from '@/lib/chunk-error.ts';

interface RouteErrorBoundaryProps {
	fullscreen?: boolean;
}

export default function RouteErrorBoundary({ fullscreen = false }: RouteErrorBoundaryProps) {
	const error = useRouteError();
	const chunk = isChunkLoadError(error);
	const [reloading, setReloading] = useState(chunk);

	useEffect(() => {
		if (!chunk) return;
		setReloading(reloadOnce());
	}, [chunk]);

	useEffect(() => {
		if (!chunk && !isRouteErrorResponse(error)) console.error(error);
	}, [chunk, error]);

	return <ErrorScreen kind={chunk ? 'chunk' : 'crash'} reloading={reloading} error={error} fullscreen={fullscreen} />;
}
