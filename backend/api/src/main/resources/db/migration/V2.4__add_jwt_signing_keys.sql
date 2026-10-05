CREATE TABLE jwt_signing_keys
(
    id          SMALLINT PRIMARY KEY CHECK (id = 1),
    public_key  TEXT        NOT NULL,
    private_key TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
