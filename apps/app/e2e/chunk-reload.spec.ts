import { expect, type Page, type Route, test } from '@playwright/test';

// Simule un onglet ouvert avant un déploiement : le chunk de la page de statut (publique, donc
// sans login) n'existe plus. Le motif couvre le dev (`/src/pages/probes/probes-status.tsx`) et
// le build (`/assets/probes-status-<hash>.js`).
const STATUS_CHUNK = /probes-status[^/]*\.(tsx|js)(\?.*)?$/;

const RELOAD_STORAGE_KEY = 'uptime-kotlin.chunk-reload-at';

function countDocumentLoads(page: Page) {
	const loads = { count: 0 };
	page.on('framenavigated', (frame) => {
		if (frame === page.mainFrame()) loads.count += 1;
	});
	return loads;
}

test('reloads once when a page chunk is gone, then recovers', async ({ page }) => {
	let failures = 0;
	await page.route(STATUS_CHUNK, async (route: Route) => {
		// Seule la première requête échoue : après rechargement, le « nouveau déploiement » répond.
		if (failures === 0) {
			failures += 1;
			return route.fulfill({ status: 404, body: 'Not Found' });
		}
		return route.continue();
	});
	const loads = countDocumentLoads(page);

	await page.goto('/status');

	await expect(page.getByRole('heading', { name: 'Health Dashboard' })).toBeVisible();
	expect(failures).toBe(1);
	expect(loads.count).toBeGreaterThanOrEqual(2);
});

test('stops reloading and offers a manual reload when the chunk stays missing', async ({ page }) => {
	await page.route(STATUS_CHUNK, (route: Route) => route.fulfill({ status: 404, body: 'Not Found' }));
	const loads = countDocumentLoads(page);

	await page.goto('/status');

	await expect(page.getByRole('heading', { name: 'A new version is available' })).toBeVisible();
	await expect(page.getByRole('button', { name: 'Reload' })).toBeVisible();
	expect(await page.evaluate((key) => sessionStorage.getItem(key), RELOAD_STORAGE_KEY)).not.toBeNull();

	// Un seul rechargement automatique : garde anti-boucle.
	await page.waitForTimeout(1_000);
	expect(loads.count).toBe(2);
});
