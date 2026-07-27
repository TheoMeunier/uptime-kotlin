-- Create table notification tasks
CREATE TABLE IF NOT EXISTS notification_tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    probe_id        UUID NOT NULL REFERENCES probes(id) ON DELETE CASCADE,
    check_task_id   UUID REFERENCES probe_check_tasks(id) ON DELETE SET NULL,
    notification_id UUID REFERENCES notifications_channels(id) ON DELETE RESTRICT,
    event           INT NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending',
    error_message    TEXT,
    channel         INT NOT NULL DEFAULT 0,
    payload         JSONB NOT NULL,
    attempt_count   INT NOT NULL DEFAULT 0,
    max_attempts    INT NOT NULL DEFAULT 5,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    claimed_by      VARCHAR(100),
    claimed_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notification_tasks_pull
    ON notification_tasks (status, next_attempt_at)
    WHERE status = 'pending';

CREATE UNIQUE INDEX IF NOT EXISTS uq_notification_tasks_check_task_channel
    ON notification_tasks (check_task_id, channel)
    WHERE check_task_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_notification_tasks_probe
    ON notification_tasks (probe_id, created_at DESC);
