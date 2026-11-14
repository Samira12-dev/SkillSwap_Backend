-- Swap completion now requires one participant to request it and the other
-- participant to confirm it.
ALTER TABLE swap_requests
    ADD COLUMN completion_requested_at DATETIME NULL;

ALTER TABLE swap_requests
    ADD COLUMN completion_requested_by BIGINT NULL;

ALTER TABLE swap_requests
    ADD CONSTRAINT fk_swap_requests_completion_requested_by
        FOREIGN KEY (completion_requested_by) REFERENCES users(id)
        ON DELETE SET NULL;
