DROP TABLE IF EXISTS worker_heartbeats;

CREATE TABLE worker_heartbeats
(
    worker_id    VARCHAR(100) PRIMARY KEY,
    region       VARCHAR(50)  NOT NULL,
    last_seen_at TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_worker_heartbeats_region_last_seen
    ON worker_heartbeats (region, last_seen_at);
