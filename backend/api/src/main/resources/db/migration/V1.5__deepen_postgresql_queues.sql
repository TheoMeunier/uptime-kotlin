-- Align the existing task tables with ADR 0003 and ADR 0004.
-- Completed rows are transient queue data; durable outcomes live in Monitor Logs.

DROP TRIGGER IF EXISTS trg_notify_probe_check_task ON probe_check_tasks;
DROP FUNCTION IF EXISTS notify_probe_check_task();

ALTER TABLE probe_check_tasks RENAME TO probe_check_jobs;

DELETE FROM probe_check_jobs
WHERE status IN ('SUCCESS', 'FAILED', 'CANCELLED');

UPDATE probe_check_jobs
SET status = 'PENDING',
    claimed_by = NULL,
    claimed_at = NULL
WHERE status = 'RUNNING';

ALTER TABLE probe_check_jobs RENAME COLUMN attempt_number TO probe_attempt;
ALTER TABLE probe_check_jobs RENAME COLUMN claimed_by TO lease_owner;
ALTER TABLE probe_check_jobs RENAME COLUMN result_message TO last_error;

ALTER TABLE probe_check_jobs
    ADD COLUMN available_at TIMESTAMPTZ,
    ADD COLUMN lease_until TIMESTAMPTZ,
    ADD COLUMN previous_failed_at TIMESTAMPTZ,
    ADD COLUMN delivery_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN max_delivery_attempts INT NOT NULL DEFAULT 5;

UPDATE probe_check_jobs SET available_at = scheduled_at;

ALTER TABLE probe_check_jobs
    ALTER COLUMN available_at SET NOT NULL,
    DROP COLUMN claimed_at;

DROP INDEX IF EXISTS idx_probe_check_tasks_pull;

CREATE UNIQUE INDEX uq_probe_check_jobs_active_probe
    ON probe_check_jobs (probe_id)
    WHERE status IN ('PENDING', 'LEASED');

CREATE INDEX idx_probe_check_jobs_claim
    ON probe_check_jobs (region, available_at, scheduled_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_probe_check_jobs_expired_lease
    ON probe_check_jobs (lease_until)
    WHERE status = 'LEASED';

ALTER TABLE notification_tasks RENAME TO notification_deliveries;

DELETE FROM notification_deliveries WHERE lower(status) = 'sent';

UPDATE notification_deliveries
SET status = CASE
    WHEN lower(status) = 'failed' THEN 'DEAD'
    ELSE 'PENDING'
END;

ALTER TABLE notification_deliveries
    DROP CONSTRAINT IF EXISTS notification_tasks_check_task_id_fkey;

ALTER TABLE notification_deliveries RENAME COLUMN check_task_id TO probe_check_job_id;
ALTER TABLE notification_deliveries RENAME COLUMN notification_id TO notification_channel_id;
ALTER TABLE notification_deliveries RENAME COLUMN attempt_count TO delivery_attempts;
ALTER TABLE notification_deliveries RENAME COLUMN max_attempts TO max_delivery_attempts;
ALTER TABLE notification_deliveries RENAME COLUMN next_attempt_at TO available_at;
ALTER TABLE notification_deliveries RENAME COLUMN claimed_by TO lease_owner;
ALTER TABLE notification_deliveries RENAME COLUMN error_message TO last_error;

ALTER TABLE notification_deliveries
    ADD COLUMN lease_until TIMESTAMPTZ,
    DROP COLUMN claimed_at,
    DROP COLUMN channel;

DROP INDEX IF EXISTS idx_notification_tasks_pull;
DROP INDEX IF EXISTS idx_notification_tasks_probe;
DROP INDEX IF EXISTS uq_notification_tasks_check_task_channel;

CREATE UNIQUE INDEX uq_notification_deliveries_job_channel
    ON notification_deliveries (probe_check_job_id, notification_channel_id)
    WHERE probe_check_job_id IS NOT NULL;

CREATE INDEX idx_notification_deliveries_claim
    ON notification_deliveries (available_at, created_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_notification_deliveries_expired_lease
    ON notification_deliveries (lease_until)
    WHERE status = 'LEASED';

CREATE INDEX idx_notification_deliveries_probe
    ON notification_deliveries (probe_id, created_at DESC);

ALTER TABLE probes_monitors_logs
    ADD COLUMN IF NOT EXISTS probe_check_job_id UUID;

CREATE UNIQUE INDEX uq_probe_monitor_logs_job
    ON probes_monitors_logs (probe_check_job_id)
    WHERE probe_check_job_id IS NOT NULL;

ALTER TABLE probes
    DROP COLUMN locked_by,
    DROP COLUMN locked_at;

DROP INDEX IF EXISTS idx_probes_scheduling;

CREATE INDEX idx_probes_scheduling
    ON probes (next_check_at)
    WHERE enabled = true;
