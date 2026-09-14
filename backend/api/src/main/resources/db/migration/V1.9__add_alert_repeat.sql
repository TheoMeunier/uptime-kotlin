-- Alert resend.
--
-- An alert that fires once, at 3am, on a channel nobody watches at that hour is
-- not an alert: the incident then lasts until morning.
--
-- Resending is not another queue. `notification_deliveries` already carries the
-- sending, the retries, the leases and the dead-letter. What was missing is a
-- DEADLINE -- an instant to recompute for as long as the alert stays open -- and
-- the probe carries it, because the probe already holds `alerted_status`, the
-- record of what the channels have been told.
--
--   alert_repeat_seconds = 0  -> no resend (current behaviour, default)
--   next_alert_at             -> next deadline; NULL = no open alert
--   alert_repeat_count        -> reminders already sent for the open alert

ALTER TABLE probes
    ADD COLUMN alert_repeat_seconds INT NOT NULL DEFAULT 0,
    ADD COLUMN next_alert_at        TIMESTAMPTZ,
    ADD COLUMN alert_repeat_count   INT NOT NULL DEFAULT 0;

-- A reminder more often than once a minute is noise, not an alert; past a day it
-- is no longer a reminder.
ALTER TABLE probes
    ADD CONSTRAINT probes_alert_repeat_seconds_valid
        CHECK (alert_repeat_seconds = 0 OR alert_repeat_seconds BETWEEN 60 AND 86400);

-- The deadline is read on every check of a failing probe, never scanned
-- globally. The index exists for operational lookups ("which probes have a
-- reminder armed?") and stays tiny.
CREATE INDEX idx_probes_alert_repeat
    ON probes (next_alert_at)
    WHERE next_alert_at IS NOT NULL;

-- Reminder rank, frozen when the delivery is queued: a delivery retried three
-- minutes later must stay "reminder #3" rather than become #4.
-- 0 = first alert or recovery.
ALTER TABLE notification_deliveries
    ADD COLUMN reminder_index INT NOT NULL DEFAULT 0;
