export default function StatusBoardFrame({ children }: { children: React.ReactNode }) {
	return (
		<div className="bg-background min-h-screen">
			<div className="mx-auto w-full max-w-7xl px-4 py-12 sm:px-6 lg:px-8">{children}</div>
		</div>
	);
}
