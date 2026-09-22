-- Remove duplicates first (keeps the oldest row) so the unique keys can be added
DELETE t1 FROM skill_details t1
INNER JOIN skill_details t2
WHERE t1.id > t2.id
  AND t1.user_id = t2.user_id
  AND t1.skill_id = t2.skill_id
  AND t1.type = t2.type;

DELETE t1 FROM reviews t1
INNER JOIN reviews t2
WHERE t1.id > t2.id
  AND t1.reviewer_id = t2.reviewer_id
  AND t1.session_id = t2.session_id;

-- Prevent duplicate skill entries for the same user/skill/type
ALTER TABLE skill_details
    ADD CONSTRAINT uk_skill_details_user_skill_type UNIQUE (user_id, skill_id, type);

-- Prevent a user from reviewing the same session more than once
ALTER TABLE reviews
    ADD CONSTRAINT uk_reviews_reviewer_session UNIQUE (reviewer_id, session_id);