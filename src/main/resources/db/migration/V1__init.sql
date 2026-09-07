-- USERS
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       first_name VARCHAR(255),
                       last_name VARCHAR(255),
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255),
                       city VARCHAR(255),
                       bio VARCHAR(1000),
                       photo VARCHAR(255),
                       rating DOUBLE,
                       created_at DATETIME,
                       role VARCHAR(50),

                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_email (email)
);

-- SKILLS
CREATE TABLE skills (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        name VARCHAR(255),
                        category VARCHAR(255),

                        PRIMARY KEY (id)
);

-- SKILL DETAILS
CREATE TABLE skill_details (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               type VARCHAR(50),
                               level VARCHAR(50),
                               user_id BIGINT NOT NULL,
                               skill_id BIGINT NOT NULL,

                               PRIMARY KEY (id),

                               CONSTRAINT fk_skill_details_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id),

                               CONSTRAINT fk_skill_details_skill
                                   FOREIGN KEY (skill_id)
                                       REFERENCES skills(id)
);

-- SWAP REQUESTS
CREATE TABLE swap_requests (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               swap_status VARCHAR(50),
                               message VARCHAR(255),
                               created_at DATETIME,

                               sender_id BIGINT,
                               receiver_id BIGINT,

                               skill_offered_id BIGINT,
                               skill_wanted_id BIGINT,

                               PRIMARY KEY (id),

                               CONSTRAINT fk_swap_sender
                                   FOREIGN KEY (sender_id)
                                       REFERENCES users(id),

                               CONSTRAINT fk_swap_receiver
                                   FOREIGN KEY (receiver_id)
                                       REFERENCES users(id),

                               CONSTRAINT fk_swap_skill_offered
                                   FOREIGN KEY (skill_offered_id)
                                       REFERENCES skills(id),

                               CONSTRAINT fk_swap_skill_wanted
                                   FOREIGN KEY (skill_wanted_id)
                                       REFERENCES skills(id)
);

-- CONVERSATIONS
CREATE TABLE conversations (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               created_at DATETIME,

                               swap_request_id BIGINT NOT NULL,

                               PRIMARY KEY (id),

                               UNIQUE KEY uk_conversation_swap_request (swap_request_id),

                               CONSTRAINT fk_conversation_swap_request
                                   FOREIGN KEY (swap_request_id)
                                       REFERENCES swap_requests(id)
);
-- MESSAGES
CREATE TABLE messages (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         content VARCHAR(2000) NOT NULL,
                         created_at DATETIME,

                         conversation_id BIGINT,
                         sender_id BIGINT,

                         PRIMARY KEY (id),

                         CONSTRAINT fk_message_conversation
                             FOREIGN KEY (conversation_id)
                                 REFERENCES conversations(id),

                         CONSTRAINT fk_message_sender
                             FOREIGN KEY (sender_id)
                                 REFERENCES users(id)
);

-- SESSIONS
CREATE TABLE sessions (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          date DATETIME,
                          duration INT,
                          mode VARCHAR(50),
                          status VARCHAR(50),

                          conversation_id BIGINT NOT NULL,

                          PRIMARY KEY (id),

                          CONSTRAINT fk_session_conversation
                              FOREIGN KEY (conversation_id)
                                  REFERENCES conversations(id)
);


--  REVIEWS
CREATE TABLE reviews (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         rating INT,
                         comment VARCHAR(255),

                         reviewer_id BIGINT NOT NULL,
                         reviewee_id BIGINT NOT NULL,
                         session_id BIGINT NOT NULL,

                         PRIMARY KEY (id),

                         CONSTRAINT fk_review_reviewer
                             FOREIGN KEY (reviewer_id)
                                 REFERENCES users(id),

                         CONSTRAINT fk_review_reviewee
                             FOREIGN KEY (reviewee_id)
                                 REFERENCES users(id),

                         CONSTRAINT fk_review_session
                             FOREIGN KEY (session_id)
                                 REFERENCES sessions(id)
);

-- NOTIFICATIONS
CREATE TABLE notifications (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               type VARCHAR(50),
                               message VARCHAR(255),
                               is_read BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at DATETIME,

                               user_id BIGINT NOT NULL,

                               PRIMARY KEY (id),

                               CONSTRAINT fk_notification_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
);