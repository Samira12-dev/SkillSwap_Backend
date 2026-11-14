-- Email verification tokens are stored as SHA-256 hashes, never in plaintext.
ALTER TABLE users
    ADD COLUMN email_verification_token_hash VARCHAR(64) NULL,
    ADD COLUMN email_verification_expires_at DATETIME NULL;

ALTER TABLE users
    ADD CONSTRAINT uk_users_email_verification_token_hash
        UNIQUE (email_verification_token_hash);
