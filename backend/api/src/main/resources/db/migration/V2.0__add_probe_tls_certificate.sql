ALTER TABLE probes
    ADD COLUMN tls_expires_at TIMESTAMPTZ,
    ADD COLUMN tls_checked_at TIMESTAMPTZ;
