import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/atoms/card.tsx';
import { Skeleton } from '@/components/atoms/skeleton.tsx';
import CreateUserDialogue from '@/features/users/components/actions/create-user-dialogue.tsx';
import UserRow from '@/features/users/components/listing/user-row.tsx';
import useUsers from '@/features/users/hooks/useUsers.ts';

export default function ListingUsers() {
	const { t } = useTranslation();
	const { data, isLoading } = useUsers();
	const adminCount = data?.filter((user) => user.role === 'ADMIN').length ?? 0;

	return (
		<Card>
			<CardHeader className="flex flex-row items-start justify-between gap-4 space-y-0">
				<div>
					<CardTitle>{t('users.title.index')}</CardTitle>
					<CardDescription className="mt-1.5">{t('users.description.index')}</CardDescription>
				</div>
				<CreateUserDialogue />
			</CardHeader>

			<CardContent>
				{isLoading || !data ? (
					<ListingSkeleton />
				) : (
					<ul className="divide-border divide-y">
						{data.map((user) => (
							<UserRow key={user.id} user={user} isLastAdmin={user.role === 'ADMIN' && adminCount <= 1} />
						))}
					</ul>
				)}
			</CardContent>
		</Card>
	);
}

function ListingSkeleton() {
	return (
		<ul className="divide-border divide-y">
			{Array.from({ length: 2 }).map((_, i) => (
				<li key={i} className="flex items-center justify-between gap-4 py-3">
					<div className="flex items-center gap-3">
						<Skeleton className="size-8 rounded-lg" />
						<div className="space-y-1.5">
							<Skeleton className="h-4 w-32" />
							<Skeleton className="h-3 w-44" />
						</div>
					</div>
					<Skeleton className="h-8 w-40 rounded-md" />
				</li>
			))}
		</ul>
	);
}
