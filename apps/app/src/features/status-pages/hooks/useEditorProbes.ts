import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import probeService from '@/features/probes/services/probeService.ts';
import type { EditorProbe } from '@/features/status-pages/lib/editor.ts';
import type { StatusPageDetail } from '@/features/status-pages/schemas/status-page.schema.ts';

export default function useEditorProbes(page: StatusPageDetail | undefined) {
	const { data: allProbes } = useQuery({ queryKey: ['probes'], queryFn: () => probeService.getProbes() });

	return useMemo(() => {
		const probes = new Map<string, EditorProbe>();

		page?.groups
			.flatMap((group) => group.probes)
			.forEach((probe) => probes.set(probe.id, { ...probe, detail: probe.url ?? probe.description }));

		allProbes?.forEach((probe) => {
			if (!probes.has(probe.id)) probes.set(probe.id, { ...probe, detail: probe.description });
		});

		return probes;
	}, [page, allProbes]);
}
