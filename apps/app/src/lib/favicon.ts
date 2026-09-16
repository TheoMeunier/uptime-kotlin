/**
 * Stamps a status dot onto the tab icon, so a tab left open in the background still says whether
 * something is broken. The badge is drawn on a canvas from the same PNG the document already
 * references, and swapped in as a data URL.
 */

const APP_ICON_HREF = '/img/logo-ui.png';
const CANVAS_SIZE = 64;

export type FaviconBadge = 'down' | 'degraded';

const BADGE_COLORS: Record<FaviconBadge, { variable: string; fallback: string }> = {
	down: { variable: '--status-down', fallback: '#d64545' },
	degraded: { variable: '--status-degraded', fallback: '#e0a030' },
};

let baseIconPromise: Promise<HTMLImageElement | null> | null = null;
let appliedBadge: FaviconBadge | null = null;
let latestRequest = 0;
const dataUrlCache = new Map<FaviconBadge, string>();

function getIconLink(): HTMLLinkElement | null {
	return document.querySelector<HTMLLinkElement>('link[rel~="icon"]');
}

function loadBaseIcon(): Promise<HTMLImageElement | null> {
	if (!baseIconPromise) {
		baseIconPromise = new Promise((resolve) => {
			const image = new Image();
			image.onload = () => resolve(image);
			image.onerror = () => resolve(null);
			image.src = APP_ICON_HREF;
		});
	}

	return baseIconPromise;
}

function resolveBadgeColor(context: CanvasRenderingContext2D, badge: FaviconBadge): string {
	const { variable, fallback } = BADGE_COLORS[badge];
	const token = getComputedStyle(document.documentElement).getPropertyValue(variable).trim();

	if (!token) return fallback;

	context.fillStyle = '#000000';
	context.fillStyle = token;

	return context.fillStyle === '#000000' ? fallback : token;
}

function drawBadgedIcon(base: HTMLImageElement, badge: FaviconBadge): string | null {
	const canvas = document.createElement('canvas');
	canvas.width = CANVAS_SIZE;
	canvas.height = CANVAS_SIZE;

	const context = canvas.getContext('2d');
	if (!context) return null;

	context.drawImage(base, 0, 0, CANVAS_SIZE, CANVAS_SIZE);

	const radius = CANVAS_SIZE * 0.27;
	const centerX = CANVAS_SIZE - radius - 2;
	const centerY = radius + 2;

	context.globalCompositeOperation = 'destination-out';
	context.beginPath();
	context.arc(centerX, centerY, radius * 1.32, 0, Math.PI * 2);
	context.fill();

	context.globalCompositeOperation = 'source-over';
	context.fillStyle = resolveBadgeColor(context, badge);
	context.beginPath();
	context.arc(centerX, centerY, radius, 0, Math.PI * 2);
	context.fill();

	return canvas.toDataURL('image/png');
}

export async function applyFaviconBadge(badge: FaviconBadge | null): Promise<void> {
	const link = getIconLink();
	if (!link || badge === appliedBadge) return;

	appliedBadge = badge;
	const request = ++latestRequest;

	if (badge === null) {
		link.href = APP_ICON_HREF;
		return;
	}

	const cached = dataUrlCache.get(badge);
	if (cached) {
		link.href = cached;
		return;
	}

	const base = await loadBaseIcon();
	if (!base || request !== latestRequest) return;

	const dataUrl = drawBadgedIcon(base, badge);
	if (!dataUrl || request !== latestRequest) return;

	dataUrlCache.set(badge, dataUrl);
	link.href = dataUrl;
}
