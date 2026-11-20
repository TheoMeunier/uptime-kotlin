CREATE INDEX idx_notification_deliveries_in_flight
    ON notification_deliveries (probe_id, notification_channel_id, created_at, id)
    WHERE status IN ('PENDING', 'LEASED');
