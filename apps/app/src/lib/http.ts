export function getFileNameFromContentDisposition(contentDisposition: string | null): string | undefined {
	const match = contentDisposition?.match(/filename="?(?<filename>[^"]+)"?/);

	return match?.groups?.filename;
}
