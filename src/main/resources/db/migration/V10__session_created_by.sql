-- Track who proposed a session so the other participant confirms it.
ALTER TABLE sessions
    ADD COLUMN created_by BIGINT NULL;

ALTER TABLE sessions
    ADD CONSTRAINT fk_sessions_created_by
        FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE SET NULL;
