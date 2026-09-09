ALTER TABLE refresh_token
    ADD COLUMN created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    ADD COLUMN last_used_at TIMESTAMP,
    ADD COLUMN user_agent   VARCHAR(512),
    ADD COLUMN ip_address   VARCHAR(45);

DELETE
FROM refresh_token a
    USING refresh_token b
WHERE a.ctid < b.ctid
  AND a.refresh_token = b.refresh_token;

CREATE UNIQUE INDEX refresh_token_token_idx ON refresh_token (refresh_token);
