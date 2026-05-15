ALTER TABLE probes
    ADD COLUMN next_check_at TIMESTAMP,
    ADD COLUMN locked_by     VARCHAR(36),
    ADD COLUMN locked_at     TIMESTAMP;

CREATE INDEX idx_probes_scheduling
    ON probes (next_check_at, enabled, locked_by);