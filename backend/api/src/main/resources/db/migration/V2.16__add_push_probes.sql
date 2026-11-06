CREATE TABLE probe_push_states
(
    probe_id          UUID PRIMARY KEY REFERENCES probes (id) ON DELETE CASCADE,
    token             VARCHAR(64) NOT NULL,
    armed_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    last_push_at      TIMESTAMPTZ,
    last_push_signal  VARCHAR(8),
    last_push_message TEXT,
    push_count        BIGINT      NOT NULL DEFAULT 0,

    CONSTRAINT uq_probe_push_states_token UNIQUE (token),
    CONSTRAINT probe_push_states_signal_known
        CHECK (last_push_signal IS NULL OR last_push_signal IN ('UP', 'DOWN')),
    CONSTRAINT probe_push_states_last_push_shape
        CHECK ((last_push_at IS NULL) = (last_push_signal IS NULL))
);
