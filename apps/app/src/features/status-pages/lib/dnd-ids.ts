import type { UniqueIdentifier } from '@dnd-kit/core';

/*
 * One drag context holds three kinds of targets. Prefixed ids tell them apart:
 * a group card, a probe row, and the drop zone of a (possibly empty) group.
 */
const GROUP = 'group:';
const PROBE = 'probe:';
const DROP = 'drop:';

export const groupDndId = (key: string) => `${GROUP}${key}`;
export const probeDndId = (id: string) => `${PROBE}${id}`;
export const dropDndId = (key: string) => `${DROP}${key}`;

export const isGroupDndId = (id: UniqueIdentifier) => String(id).startsWith(GROUP);
export const isProbeDndId = (id: UniqueIdentifier) => String(id).startsWith(PROBE);
export const isDropDndId = (id: UniqueIdentifier) => String(id).startsWith(DROP);

export const groupKeyOf = (id: UniqueIdentifier) => String(id).slice(GROUP.length);
export const probeIdOf = (id: UniqueIdentifier) => String(id).slice(PROBE.length);
export const dropKeyOf = (id: UniqueIdentifier) => String(id).slice(DROP.length);
