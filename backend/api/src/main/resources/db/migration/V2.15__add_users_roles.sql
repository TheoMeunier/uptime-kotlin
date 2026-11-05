ALTER TABLE users
    ADD COLUMN role SMALLINT NOT NULL DEFAULT 0
        CONSTRAINT users_role_check CHECK (role IN (0, 1));
