self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', (event) => event.waitUntil(self.clients.claim()));

self.addEventListener('push', (event) => {
	let data = {};
	try {
		data = event.data ? event.data.json() : {};
	} catch {
		data = { body: event.data ? event.data.text() : '' };
	}

	const title = data.title || 'Uptime Kotlin';
	event.waitUntil(
		self.registration.showNotification(title, {
			body: data.body || '',
			tag: data.tag || undefined,
			renotify: Boolean(data.tag),
			requireInteraction: Boolean(data.require_interaction),
			icon: '/img/logo-ui.png',
			badge: '/img/logo-ui.png',
			data: { url: data.url || '/' },
		})
	);
});

self.addEventListener('notificationclick', (event) => {
	event.notification.close();
	const target = new URL(event.notification.data?.url || '/', self.location.origin).href;

	event.waitUntil(
		(async () => {
			const windows = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
			const existing = windows.find((client) => new URL(client.url).origin === self.location.origin);
			if (!existing) return self.clients.openWindow(target);

			const focused = await existing.focus();
			// navigate() only works on a window this worker controls; otherwise the focused tab is enough.
			return focused.navigate(target).catch(() => focused);
		})()
	);
});
