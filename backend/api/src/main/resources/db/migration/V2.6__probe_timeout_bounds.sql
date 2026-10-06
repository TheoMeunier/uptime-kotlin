UPDATE probes SET timeout = 5 WHERE timeout <= 0;
UPDATE probes SET timeout = 120 WHERE timeout > 120;

ALTER TABLE probes
    ALTER COLUMN timeout SET DEFAULT 5,
    ADD CONSTRAINT probes_timeout_valid CHECK (timeout BETWEEN 1 AND 120);
