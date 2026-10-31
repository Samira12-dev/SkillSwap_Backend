ALTER TABLE sessions
    ADD COLUMN completion_requested_at DATETIME NULL,
    ADD COLUMN completion_requested_by BIGINT NULL,
    ADD COLUMN completion_disputed_at DATETIME NULL,
    ADD COLUMN completed_at DATETIME NULL;

ALTER TABLE sessions
    ADD CONSTRAINT fk_session_completion_requested_by
        FOREIGN KEY (completion_requested_by) REFERENCES users(id);

CREATE INDEX idx_sessions_status ON sessions(status);
CREATE INDEX idx_sessions_date ON sessions(date);
