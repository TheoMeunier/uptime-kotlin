/**
 * Public status boards poll on their own, so a query is considered fresh for the whole interval:
 * coming back to the tab does not fire a second round of requests on top of the polling.
 */
export const STATUS_REFRESH_MS = 120_000;

export const STATUS_QUERY_TIMING = {
	refetchInterval: STATUS_REFRESH_MS,
	staleTime: STATUS_REFRESH_MS,
} as const;
