ALTER TABLE probes
    ADD COLUMN failing_since TIMESTAMPTZ;

ALTER TABLE notification_deliveries
    ADD COLUMN downtime_seconds BIGINT;
