import { Component, type ErrorInfo, type ReactNode } from 'react';
import ErrorScreen from '@/components/molecules/error-screen.tsx';
import { isChunkLoadError, reloadOnce } from '@/lib/chunk-error.ts';

interface AppErrorBoundaryProps {
	children: ReactNode;
}

interface AppErrorBoundaryState {
	error: unknown;
	hasError: boolean;
	reloading: boolean;
}

export default class AppErrorBoundary extends Component<AppErrorBoundaryProps, AppErrorBoundaryState> {
	state: AppErrorBoundaryState = { error: undefined, hasError: false, reloading: false };

	static getDerivedStateFromError(error: unknown): Partial<AppErrorBoundaryState> {
		return { error, hasError: true, reloading: isChunkLoadError(error) };
	}

	componentDidCatch(error: unknown, info: ErrorInfo) {
		if (isChunkLoadError(error)) {
			this.setState({ reloading: reloadOnce() });
			return;
		}
		console.error(error, info.componentStack);
	}

	render() {
		if (!this.state.hasError) return this.props.children;

		const chunk = isChunkLoadError(this.state.error);
		return (
			<ErrorScreen
				kind={chunk ? 'chunk' : 'crash'}
				reloading={chunk && this.state.reloading}
				error={this.state.error}
				fullscreen
			/>
		);
	}
}
