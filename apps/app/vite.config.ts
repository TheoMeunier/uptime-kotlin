import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';

// https://vite.dev/config/
export default defineConfig({
	plugins: [react(), tailwindcss()],
	define: {
		__DEMO_BUILD__: JSON.stringify(process.env.VITE_DEMO === 'true'),
	},
	resolve: {
		alias: {
			'@': path.resolve(__dirname, './src'),
		},
	},
});
