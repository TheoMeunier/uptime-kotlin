import { z } from 'zod';

export const VersionSchema = z.object({
	version: z.string(),
});

export type Version = z.infer<typeof VersionSchema>;
