export function getFileNameFromContentDisposition(contentDisposition: string | null): string | undefined {
	const match = contentDisposition?.match(/filename="?(?<filename>[^"]+)"?/);

	return match?.groups?.filename;
}

export function downloadBlob(blob: Blob, fileName: string) {
	const url = URL.createObjectURL(blob);
	const link = document.createElement('a');

	link.href = url;
	link.download = fileName;
	link.click();
	URL.revokeObjectURL(url);
}
