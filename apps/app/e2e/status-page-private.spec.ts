import { expect, type Page, test } from '@playwright/test';

const ADMIN = {
	name: process.env.E2E_NAME ?? 'E2E Admin',
	email: process.env.E2E_EMAIL ?? 'e2e@uptime-kotlin.test',
	password: process.env.E2E_PASSWORD ?? 'e2e-password-123',
};

const ONBOARDING_KEY = `uptime-kotlin.onboarding.completed.${ADMIN.email}`;

async function skipOnboarding(page: Page) {
	await page.addInitScript((key) => window.localStorage.setItem(key, 'true'), ONBOARDING_KEY);
}

async function completeSetupIfNeeded(page: Page) {
	await skipOnboarding(page);
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

async function fillLogin(page: Page) {
	await page.getByLabel('Email').fill(ADMIN.email);
	await page.getByLabel('Password', { exact: true }).fill(ADMIN.password);
	await page.getByRole('button', { name: 'Login', exact: true }).click();
}

test('a private status page looks missing to anonymous visitors, then is served once signed in', async ({
	page,
	browser,
}) => {
	const title = `E2E private ${Date.now()}`;

	await completeSetupIfNeeded(page);
	await page.goto('/login');
	await fillLogin(page);
	await page.waitForURL(/\/dashboard/);

	await page.goto('/status-pages/new');
	await page.getByLabel('Title').fill(title);
	await page.getByLabel('Private page').click();
	await page.getByRole('button', { name: 'Save', exact: true }).click();
	await page.waitForURL(/\/status-pages\/[0-9a-f-]+$/);
	const slug = await page.getByLabel('Address').inputValue();

	await page.goto(`/status/${slug}`);
	await expect(page.getByText(title)).toBeVisible();

	const visitorContext = await browser.newContext();
	const visitor = await visitorContext.newPage();
	await skipOnboarding(visitor);
	await visitor.goto(`/status/${slug}`);
	await expect(visitor.getByText('Status page not found')).toBeVisible();
	await expect(visitor.getByText(title)).toHaveCount(0);

	await visitor.getByRole('link', { name: 'Login' }).click();
	await visitor.waitForURL(/\/login\?redirect=/);
	await fillLogin(visitor);
	await visitor.waitForURL(new RegExp(`/status/${slug}$`));
	await expect(visitor.getByText(title)).toBeVisible();

	await visitorContext.close();
});
