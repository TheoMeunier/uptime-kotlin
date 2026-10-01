-- Probe log retention.
--
-- `probes_monitors_logs` grows by one row per probe and per check, forever: the
-- only way to shrink it was the manual purge endpoint. Retention is now a
-- duration, set once for the instance and overridable probe by probe.
--
--   instance_settings.log_retention_days  -> instance default; NULL = keep forever
--   probes.log_retention_days             -> override; NULL = inherit the default,
--                                            0 = keep forever whatever the default
--
-- Effective retention = COALESCE(probe, instance), where 0 means "keep forever".
-- NULL on both sides means no purge, and that is what every existing installation gets after upgrading:
-- nobody wakes up to a history that melted overnight.
--
-- Floor of 30 days: the 24h / 7d / 30d uptimes are computed from raw logs, so a
-- shorter retention would silently falsify the 30-day figure.

CREATE TABLE instance_settings
(
    id                 SMALLINT PRIMARY KEY CHECK (id = 1),
    log_retention_days INT,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT instance_settings_log_retention_valid
        CHECK (log_retention_days IS NULL OR log_retention_days BETWEEN 30 AND 3650)
);

INSERT INTO instance_settings (id, log_retention_days)
VALUES (1, NULL);

ALTER TABLE probes
    ADD COLUMN log_retention_days INT,
    ADD CONSTRAINT probes_log_retention_valid
        CHECK (log_retention_days IS NULL OR log_retention_days = 0 OR log_retention_days BETWEEN 30 AND 3650);
