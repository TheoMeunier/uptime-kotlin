ALTER TABLE users
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE instance_settings
    ADD COLUMN admin_password_reset_email VARCHAR(255);
