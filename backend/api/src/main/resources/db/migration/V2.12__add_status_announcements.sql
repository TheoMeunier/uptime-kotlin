ALTER TABLE status_pages
    ADD COLUMN uptime_period VARCHAR(4) NOT NULL DEFAULT 'H1',
    ADD CONSTRAINT status_pages_uptime_period_known CHECK (uptime_period IN ('H1', 'D1', 'D7', 'D30', 'D60', 'D90'));

CREATE TABLE status_announcements
(
    id             UUID PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    body           TEXT,
    severity       VARCHAR(16)  NOT NULL DEFAULT 'INFO',
    starts_at      TIMESTAMPTZ  NOT NULL,
    ends_at        TIMESTAMPTZ,
    show_on_global BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT status_announcements_severity_known CHECK (severity IN ('INFO', 'DEGRADED', 'OUTAGE', 'MAINTENANCE')),
    CONSTRAINT status_announcements_window_ordered CHECK (ends_at IS NULL OR ends_at >= starts_at)
);

CREATE INDEX idx_status_announcements_window
    ON status_announcements (starts_at, ends_at);

CREATE TABLE status_announcement_pages
(
    announcement_id UUID NOT NULL REFERENCES status_announcements (id) ON DELETE CASCADE,
    status_page_id  UUID NOT NULL REFERENCES status_pages (id) ON DELETE CASCADE,

    PRIMARY KEY (announcement_id, status_page_id)
);

CREATE INDEX idx_status_announcement_pages_page
    ON status_announcement_pages (status_page_id);
