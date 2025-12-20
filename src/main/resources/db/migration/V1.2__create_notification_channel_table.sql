CREATE TABLE notifications_channels
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    type       INT                   DEFAULT 0,
    is_default BOOLEAN               DEFAULT FALSE,
    content    JSONB                 DEFAULT NULL,

    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE probes_notifications_channels
(
    probe_id                UUID REFERENCES probes (id),
    notification_channel_id UUID REFERENCES notifications_channels (id),

    PRIMARY KEY (probe_id, notification_channel_id),
    FOREIGN KEY (probe_id) REFERENCES probes (id) ON DELETE CASCADE,
    FOREIGN KEY (notification_channel_id) REFERENCES notifications_channels (id) ON DELETE CASCADE
);

