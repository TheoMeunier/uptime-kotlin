CREATE TABLE incidents
(
    id                    UUID PRIMARY KEY,
    probe_id              UUID        NOT NULL REFERENCES probes (id) ON DELETE CASCADE,

    started_at            TIMESTAMPTZ NOT NULL,
    confirmed_at          TIMESTAMPTZ NOT NULL,
    ended_at              TIMESTAMPTZ,
    duration_seconds      BIGINT,

    opening_message       TEXT        NOT NULL,
    opening_check_task_id UUID,
    closing_check_task_id UUID,
    closed_reason         VARCHAR(16),

    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT incidents_confirmed_after_start CHECK (confirmed_at >= started_at),
    CONSTRAINT incidents_ended_after_start CHECK (ended_at IS NULL OR ended_at >= started_at),
    CONSTRAINT incidents_closed_shape CHECK (
        (ended_at IS NULL AND duration_seconds IS NULL AND closed_reason IS NULL) OR
        (ended_at IS NOT NULL AND duration_seconds >= 0 AND closed_reason IN ('RECOVERY', 'PROBE_DISABLED')))
);

CREATE UNIQUE INDEX uq_incidents_open_per_probe
    ON incidents (probe_id) WHERE ended_at IS NULL;

CREATE INDEX idx_incidents_probe_started
    ON incidents (probe_id, started_at DESC);

CREATE INDEX idx_incidents_started
    ON incidents (started_at DESC);

INSERT INTO incidents (id, probe_id, started_at, confirmed_at, opening_message)
SELECT gen_random_uuid(),
       p.id,
       LEAST(COALESCE(p.failing_since, p.last_run, now()), COALESCE(p.last_run, now())),
       COALESCE(p.last_run, now()),
       COALESCE((SELECT l.message
                 FROM probes_monitors_logs l
                 WHERE l.probe_id = p.id
                 ORDER BY l.run_at DESC
                 LIMIT 1), '')
FROM probes p
WHERE p.enabled = true
  AND p.alerted_status = 3;
