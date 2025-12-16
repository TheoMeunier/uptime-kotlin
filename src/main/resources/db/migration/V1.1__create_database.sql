CREATE TABLE probes
(
    id                        UUID PRIMARY KEY,
    name                      VARCHAR(255) NOT NULL,
    url                       VARCHAR(255) NOT NULL,
    interval                  INT          NOT NULL,
    timeout                   INT          NOT NULL,
    retry                     INT          NOT NULL,
    interval_retry            INT          NOT NULL,
    enabled                   BOOLEAN      NOT NULL,
    protocol                  INT          NOT NULL DEFAULT 0,
    description               TEXT                  DEFAULT NULL,
    last_run                  TIMESTAMP             DEFAULT NULL,

    notification_certified    BOOLEAN      NOT NULL DEFAULT FALSE,
    ignore_certificate_errors BOOLEAN      NOT NULL DEFAULT FALSE,
    http_code_allowed         VARCHAR               DEFAULT NULL,

    tcp_port                  INT                   DEFAULT NULL,

    dns_port                  INT                   DEFAULT NULL,
    dns_server                VARCHAR(255)          default NULL,

    ping_max_packet           INT                   DEFAULT NULL,
    ping_size                 INT                   DEFAULT NULL,
    ping_delay                INT                   DEFAULT NULL,
    ping_numeric_output       BOOLEAN               DEFAULT NULL,

    created_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
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
