CREATE TABLE status_pages
(
    id             UUID PRIMARY KEY,
    slug           VARCHAR(64)  NOT NULL,
    title          VARCHAR(255) NOT NULL,
    description    TEXT,
    default_layout VARCHAR(8)   NOT NULL DEFAULT 'GRID',

    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_status_pages_slug UNIQUE (slug),
    CONSTRAINT status_pages_slug_format CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    CONSTRAINT status_pages_layout_known CHECK (default_layout IN ('GRID', 'LIST'))
);

CREATE TABLE status_page_groups
(
    id             UUID PRIMARY KEY,
    status_page_id UUID    NOT NULL REFERENCES status_pages (id) ON DELETE CASCADE,
    name           VARCHAR(255),
    position       INTEGER NOT NULL,

    CONSTRAINT status_page_groups_position_positive CHECK (position >= 0)
);

CREATE INDEX idx_status_page_groups_page
    ON status_page_groups (status_page_id, position);

CREATE TABLE status_page_group_probes
(
    group_id UUID    NOT NULL REFERENCES status_page_groups (id) ON DELETE CASCADE,
    probe_id UUID    NOT NULL REFERENCES probes (id) ON DELETE CASCADE,
    position INTEGER NOT NULL,

    PRIMARY KEY (group_id, probe_id),
    CONSTRAINT status_page_group_probes_position_positive CHECK (position >= 0)
);

CREATE INDEX idx_status_page_group_probes_probe
    ON status_page_group_probes (probe_id);
