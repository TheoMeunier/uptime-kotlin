import { z } from 'zod';

export const DefaultStatusPageSettingsSchema = z.object({
	is_private: z.boolean(),
});

export type DefaultStatusPageSettings = z.infer<typeof DefaultStatusPageSettingsSchema>;
