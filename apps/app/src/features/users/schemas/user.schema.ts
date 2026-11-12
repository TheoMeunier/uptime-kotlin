import { z } from 'zod';

export const UserRoleEnum = ['ADMIN', 'VIEWER'] as const;
export type UserRole = (typeof UserRoleEnum)[number];

export const UserSchema = z.object({
	id: z.uuid(),
	name: z.string(),
	email: z.string(),
	role: z.enum(UserRoleEnum),
	created_at: z.string(),
	current: z.boolean(),
	must_change_password: z.boolean().default(false),
});

export type User = z.infer<typeof UserSchema>;

export const UserListSchema = z.array(UserSchema);

export const ResetPasswordResponseSchema = z.object({
	password: z.string().min(1),
});
