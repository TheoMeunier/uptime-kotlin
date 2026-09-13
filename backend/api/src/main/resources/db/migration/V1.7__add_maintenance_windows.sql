-- Maintenance windows.
--
-- A maintenance window is a TIME INTERVAL, not a state: nothing ever "enters" or
-- "leaves" maintenance. Every read asks whether a given instant falls inside an
-- occurrence, so a window that expires while the cluster is down leaves nothing
-- behind to repair.
--
-- Recurrence rules are unrolled into `maintenance_occurrences` by a scheduled job.
-- Past occurrences are never recomputed: they are the record of what was planned.

CREATE TABLE maintenance_windows
(
    id               UUID PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    description      TEXT,

    -- Rule
    starts_at        TIMESTAMPTZ  NOT NULL,
    duration_s       INTEGER      NOT NULL,
    recurrence       VARCHAR(16)  NOT NULL DEFAULT 'ONCE',
    recurrence_until TIMESTAMPTZ,
    timezone         VARCHAR(64)  NOT NULL DEFAULT 'UTC',

    -- Control
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    is_public        BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- A window with no end is a monitoring system silently disarmed.
    CONSTRAINT maintenance_windows_duration_positive CHECK (duration_s > 0),
    CONSTRAINT maintenance_windows_recurrence_known
        CHECK (recurrence IN ('ONCE', 'DAILY', 'WEEKLY', 'MONTHLY'))
);

CREATE INDEX idx_maintenance_windows_materializable
    ON maintenance_windows (starts_at)
    WHERE active;

CREATE TABLE maintenance_window_probes
(
    maintenance_window_id UUID NOT NULL REFERENCES maintenance_windows (id) ON DELETE CASCADE,
    probe_id              UUID NOT NULL REFERENCES probes (id) ON DELETE CASCADE,

    PRIMARY KEY (maintenance_window_id, probe_id)
);

CREATE INDEX idx_maintenance_window_probes_probe
    ON maintenance_window_probes (probe_id);

CREATE TABLE maintenance_occurrences
(
    id         UUID PRIMARY KEY,
    window_id  UUID        NOT NULL REFERENCES maintenance_windows (id) ON DELETE CASCADE,
    starts_at  TIMESTAMPTZ NOT NULL,
    ends_at    TIMESTAMPTZ NOT NULL,
    cancelled  BOOLEAN     NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT maintenance_occurrences_ordered CHECK (ends_at > starts_at),
    CONSTRAINT uq_maintenance_occurrences_window_start UNIQUE (window_id, starts_at)
);

-- Serves the hot path ("is this instant inside an occurrence?"), the "next
-- maintenance" lookup, and the calendar.
CREATE INDEX idx_maintenance_occurrences_active
    ON maintenance_occurrences (starts_at, ends_at)
    WHERE NOT cancelled;

CREATE INDEX idx_maintenance_occurrences_window
    ON maintenance_occurrences (window_id, starts_at);

-- Maintenance is a DIMENSION of a measurement, not a status: `status` keeps
-- saying what the service was worth, `under_maintenance` says under which regime
-- the measurement was taken. Stamping it at write time freezes history — editing
-- a window later cannot rewrite yesterday's uptime.
ALTER TABLE probes_monitors_logs
    ADD COLUMN under_maintenance BOOLEAN NOT NULL DEFAULT FALSE;

-- Observed status (`status`) vs. alerted status (`alerted_status`): what the
-- probe measures vs. what the notification recipients have been told. Without
-- this split, an outage that starts inside a window and outlives it is never
-- announced (FAILURE -> FAILURE resolves to NONE).
ALTER TABLE probes
    ADD COLUMN alerted_status INT NOT NULL DEFAULT 0;

UPDATE probes
SET alerted_status = COALESCE(status, 0);
