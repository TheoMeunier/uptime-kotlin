export default function LoaderPage() {
	return (
		<div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-white via-gray-50 to-indigo-50">
			<div className="flex flex-col items-center gap-6">
				<div className="relative">
					<img
						src="img/logo.png"
						alt="Uptime Kotlin logo"
						className="relative w-24 h-24 md:w-32 md:h-32 drop-shadow-xl"
					/>
				</div>

				<div className="text-center space-y-2">
					<h1 className="text-3xl md:text-4xl font-semibold tracking-tight text-gray-800">Uptime Kotlin</h1>
				</div>

				<div className="w-56 h-1.5 bg-gray-200 rounded-full overflow-hidden">
					<div className="h-full w-1/3 bg-gradient-to-r from-indigo-400 via-indigo-500 to-purple-400 animate-[loader_1.4s_ease-in-out_infinite]" />
				</div>

				<span className="sr-only">Chargement en cours</span>
			</div>
		</div>
	);
}
