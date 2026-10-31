-- Account moderation fields
ALTER TABLE users
    ADD COLUMN phone VARCHAR(30) NULL,
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE users
    ADD CONSTRAINT uk_users_phone UNIQUE (phone);

-- Reports submitted by users
CREATE TABLE reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    admin_note VARCHAR(1000),
    handled_by_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    handled_at DATETIME,

    PRIMARY KEY (id),

    CONSTRAINT fk_report_reporter
        FOREIGN KEY (reporter_id) REFERENCES users(id),

    CONSTRAINT fk_report_reported_user
        FOREIGN KEY (reported_user_id) REFERENCES users(id),

    CONSTRAINT fk_report_handled_by
        FOREIGN KEY (handled_by_id) REFERENCES users(id),

    INDEX idx_reports_status (status),
    INDEX idx_reports_reason (reason),
    INDEX idx_reports_reporter (reporter_id),
    INDEX idx_reports_reported_user (reported_user_id)
);

-- Ban / suspension audit history
CREATE TABLE user_bans (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    admin_id BIGINT NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    ban_type VARCHAR(20) NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    lifted_at DATETIME,
    lifted_by_id BIGINT,

    PRIMARY KEY (id),

    CONSTRAINT fk_user_ban_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_user_ban_admin
        FOREIGN KEY (admin_id) REFERENCES users(id),

    CONSTRAINT fk_user_ban_lifted_by
        FOREIGN KEY (lifted_by_id) REFERENCES users(id),

    INDEX idx_user_bans_user (user_id),
    INDEX idx_user_bans_active (active)
);

-- Administrative audit trail
CREATE TABLE admin_actions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    target_user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    details VARCHAR(1000),
    created_at DATETIME NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_admin_action_admin
        FOREIGN KEY (admin_id) REFERENCES users(id),

    CONSTRAINT fk_admin_action_target
        FOREIGN KEY (target_user_id) REFERENCES users(id),

    INDEX idx_admin_actions_target (target_user_id),
    INDEX idx_admin_actions_created (created_at)
);
