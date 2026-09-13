CREATE INDEX IF NOT EXISTS idx_probes_monitors_logs_probe_run_at
    ON probes_monitors_logs (probe_id, run_at);

CREATE INDEX IF NOT EXISTS idx_probes_monitors_logs_run_at
    ON probes_monitors_logs (run_at);

DROP INDEX IF EXISTS probes_monitors_logs_id_idx;

DROP INDEX IF EXISTS probes_monitors_logs_probe_id_idx;

DROP INDEX IF EXISTS probes_name_idx;
