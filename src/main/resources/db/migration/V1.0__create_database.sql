CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX users_email_idx ON users (email);

CREATE TABLE refresh_token
(
    id            UUID PRIMARY KEY,
    user_id       UUID      NOT NULL,
    refresh_token UUID      NOT NULL,
    expired_at    TIMESTAMP NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX refresh_token_user_id_idx ON refresh_token (user_id);
CREATE INDEX refresh_token_expired_at_idx ON refresh_token (expired_at);