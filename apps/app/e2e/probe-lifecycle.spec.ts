import { expect, type Page, test } from '@playwright/test';

const ADMIN = {
	name: process.env.E2E_NAME ?? 'E2E Admin',
	email: process.env.E2E_EMAIL ?? 'e2e@uptime-kotlin.test',
	password: process.env.E2E_PASSWORD ?? 'e2e-password-123',
};

async function completeSetupIfNeeded(page: Page) {
	await page.goto('/');
	await page.waitForURL(/\/(setup|login|dashboard)/);

	if (!page.url().includes('/setup')) return;

	await page.getByLabel('Full Name').fill(ADMIN.name);
	await page.getByLabel('Email').fill(ADMIN.email);
	await page.getByLabel('Password', { exact: true }).fill(ADMIN.password);
	await page.getByLabel('Confirmation Password').fill(ADMIN.password);
	await page.getByRole('button', { name: 'Create first account' }).click();

	await page.waitForURL(/\/login/);
}

async function login(page: Page) {
	if (!page.url().includes('/login')) {
		await page.goto('/login');
	}

	await page.getByLabel('Email').fill(ADMIN.email);
	await page.getByLabel('Password', { exact: true }).fill(ADMIN.password);
	await page.getByRole('button', { name: 'Login', exact: true }).click();

	await page.waitForURL(/\/dashboard/);
}

test('setup, login, create a monitor and find it in the sidebar', async ({ page }) => {
	const probeName = `E2E monitor ${Date.now()}`;

	await completeSetupIfNeeded(page);
	await login(page);

	await page.getByRole('link', { name: 'New monitor' }).click();
	await page.waitForURL(/\/monitors\/new/);

	await page.getByLabel('Monitor name').fill(probeName);
	await page.getByPlaceholder('https://').fill('https://example.com');
	await page.getByLabel('Check interval (s)').fill('60');

	await page.getByRole('button', { name: 'Create monitor' }).click();

	await page.waitForURL(/\/dashboard/);

	const sidebarLink = page.getByRole('link', { name: probeName });
	await expect(sidebarLink).toBeVisible();

	await sidebarLink.click();
	await page.waitForURL(/\/monitors\/[0-9a-f-]+$/);
	await expect(page.getByRole('heading', { name: probeName })).toBeVisible();
	await expect(page.getByText('Check every 60 seconds')).toBeVisible();
});

test('duplicate a monitor, adjust the target and create the copy', async ({ page }) => {
	const sourceName = `E2E source ${Date.now()}`;
	const copyName = `${sourceName} (copy)`;

	await completeSetupIfNeeded(page);
	await login(page);

	await page.getByRole('link', { name: 'New monitor' }).click();
	await page.getByLabel('Monitor name').fill(sourceName);
	await page.getByPlaceholder('https://').fill('https://example.com/health');
	await page.getByLabel('Check interval (s)').fill('120');
	await page.getByRole('button', { name: 'Create monitor' }).click();
	await page.waitForURL(/\/dashboard/);

	await page.getByRole('link', { name: sourceName }).click();
	await page.waitForURL(/\/monitors\/[0-9a-f-]+$/);

	await page.getByRole('button', { name: 'More actions' }).click();
	await page.getByRole('menuitem', { name: 'Duplicate' }).click();
	await page.waitForURL(/\/monitors\/new\?from=[0-9a-f-]+$/);

	await expect(page.getByRole('heading', { name: 'Duplicate monitor' })).toBeVisible();
	await expect(page.getByLabel('Monitor name')).toHaveValue(copyName);
	await expect(page.getByLabel('Check interval (s)')).toHaveValue('120');

	await page.getByPlaceholder('https://').fill('https://example.com/status');
	await page.getByRole('button', { name: 'Create monitor' }).click();
	await page.waitForURL(/\/dashboard/);

	// La source et sa copie : le nom de la source est un préfixe de celui de la copie.
	await expect(page.getByRole('link', { name: sourceName })).toHaveCount(2);
	await expect(page.getByRole('link', { name: copyName })).toBeVisible();
});
