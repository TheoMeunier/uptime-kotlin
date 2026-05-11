CREATE TABLE probes
(
    id             UUID PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    interval       INT          NOT NULL,
    timeout        INT          NOT NULL,
    retry          INT          NOT NULL,
    interval_retry INT          NOT NULL,
    enabled        BOOLEAN      NOT NULL,
    protocol       INT          NOT NULL DEFAULT 0,
    status         INT                   DEFAULT NULL,
    description    TEXT                  DEFAULT NULL,
    last_run       TIMESTAMP             DEFAULT NULL,
    content        JSONB                 DEFAULT NULL,

    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX probes_name_idx ON probes (id);

CREATE TABLE probes_monitors_logs
(
    id            UUID PRIMARY KEY,
    probe_id      UUID           NOT NULL,
    status        INT  default 0 NOT NULL,
    response_time INT  default 0 NOT NULL,
    message       TEXT DEFAULT NULL,
    run_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (probe_id) REFERENCES probes (id) ON DELETE CASCADE
);

CREATE INDEX probes_monitors_logs_id_idx ON probes_monitors_logs (id);
CREATE INDEX probes_monitors_logs_probe_id_idx ON probes_monitors_logs (probe_id);

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

