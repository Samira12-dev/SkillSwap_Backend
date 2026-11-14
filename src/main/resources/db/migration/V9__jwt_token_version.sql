-- Incrementing this value revokes all previously issued JWTs for the user.
ALTER TABLE users
    ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0;
