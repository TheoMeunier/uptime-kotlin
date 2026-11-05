import { useTranslation } from 'react-i18next';
import { Eye, ShieldCheck } from 'lucide-react';
import { Badge } from '@/components/atoms/badge.tsx';
import type { UserRole } from '@/features/users/schemas/user.schema.ts';

export default function UserRoleBadge({ role }: { role: UserRole }) {
	const { t } = useTranslation();
	const Icon = role === 'ADMIN' ? ShieldCheck : Eye;

	return (
		<Badge variant={role === 'ADMIN' ? 'secondary' : 'outline'} className="font-normal">
			<Icon />
			{t(`users.role.${role}`)}
		</Badge>
	);
}
