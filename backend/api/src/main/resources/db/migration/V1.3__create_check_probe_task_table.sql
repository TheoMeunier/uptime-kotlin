-- Update table probe
ALTER TABLE probes
    ADD COLUMN IF NOT EXISTS regions_order JSONB NOT NULL DEFAULT '[]'::jsonb;


-- Create table check tasks for probe checks.
CREATE TABLE IF NOT EXISTS probe_check_tasks (
    id              UUID PRIMARY KEY,
    probe_id      UUID NOT NULL REFERENCES probes(id) ON DELETE CASCADE,
    region          VARCHAR(50) NOT NULL,
    attempt_number  INT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending / running / success / failed / cancelled
    scheduled_at    TIMESTAMPTZ NOT NULL,
    claimed_by      VARCHAR(100),
    claimed_at      TIMESTAMPTZ,
    result_message  TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
    );

-- Index critical for pulling tasks by region and status.
CREATE INDEX IF NOT EXISTS idx_probe_check_tasks_pull
    ON probe_check_tasks (region, status, scheduled_at)
    WHERE status = 'pending';

CREATE UNIQUE INDEX IF NOT EXISTS uq_probe_check_tasks_active_per_probe
    ON probe_check_tasks (probe_id)
    WHERE status IN ('pending', 'running');

-- Notification native Postgres.
CREATE OR REPLACE FUNCTION notify_probe_check_task() RETURNS trigger AS $$
BEGIN
    PERFORM pg_notify('check_tasks_' || NEW.region, NEW.id::text);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_notify_probe_check_task ON probe_check_tasks;

CREATE TRIGGER trg_notify_probe_check_task
    AFTER INSERT ON probe_check_tasks
    FOR EACH ROW
    EXECUTE FUNCTION notify_probe_check_task();

-- Create table for probe check results.
CREATE TABLE IF NOT EXISTS worker_heartbeats (
   region VARCHAR(50) PRIMARY KEY,
   worker_id VARCHAR(100) NOT NULL,
   last_seen_at TIMESTAMPTZ NOT NULL
);