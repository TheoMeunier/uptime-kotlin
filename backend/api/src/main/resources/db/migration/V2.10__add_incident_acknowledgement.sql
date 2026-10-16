ALTER TABLE incidents
    ADD COLUMN acknowledged_at TIMESTAMPTZ,
    ADD COLUMN acknowledged_by UUID REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN note            TEXT,
    ADD COLUMN note_updated_at TIMESTAMPTZ;
