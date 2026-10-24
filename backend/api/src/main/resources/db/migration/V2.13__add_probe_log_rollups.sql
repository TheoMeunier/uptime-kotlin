CREATE TABLE probe_logs_hourly
(
    probe_id            UUID        NOT NULL REFERENCES probes (id) ON DELETE CASCADE,
    bucket_start        TIMESTAMPTZ NOT NULL,

    checks_total        INT         NOT NULL,
    checks_success      INT         NOT NULL,
    checks_warning      INT         NOT NULL,
    checks_failure      INT         NOT NULL,
    checks_maintenance  INT         NOT NULL,

    response_time_sum   BIGINT      NOT NULL,
    response_time_count INT         NOT NULL,
    response_time_min   INT,
    response_time_max   INT,

    last_success_at     TIMESTAMPTZ,
    computed_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (probe_id, bucket_start)
);

CREATE TABLE probe_log_rollup_state
(
    id              SMALLINT PRIMARY KEY CHECK (id = 1),
    rolled_up_until TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO probe_log_rollup_state (id, rolled_up_until)
VALUES (1, NULL);

ALTER TABLE instance_settings
    DROP CONSTRAINT instance_settings_log_retention_valid,
    ADD CONSTRAINT instance_settings_log_retention_valid
        CHECK (log_retention_days IS NULL OR log_retention_days BETWEEN 7 AND 3650);

ALTER TABLE probes
    DROP CONSTRAINT probes_log_retention_valid,
    ADD CONSTRAINT probes_log_retention_valid
        CHECK (log_retention_days IS NULL OR log_retention_days = 0 OR log_retention_days BETWEEN 7 AND 3650);
