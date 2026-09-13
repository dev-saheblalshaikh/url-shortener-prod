USE url_shortener;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(190) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_users_email (email)
);

-- Run this only if your existing short_urls table was created before auth.
-- It creates a temporary owner for old links so Hibernate validation can pass.
INSERT INTO app_users (email, password_hash)
SELECT 'admin@example.com', '$2a$10$mJ5AsUqZXl1aCjSK6Y9hXu2dC5cHnw/PnvQfkrE9ExQCdqxeQDBQy'
WHERE NOT EXISTS (
    SELECT 1 FROM app_users WHERE email = 'admin@example.com'
);

SET @admin_id = (SELECT id FROM app_users WHERE email = 'admin@example.com');

ALTER TABLE short_urls ADD COLUMN user_id BIGINT NULL;
UPDATE short_urls SET user_id = @admin_id WHERE user_id IS NULL;
ALTER TABLE short_urls MODIFY COLUMN user_id BIGINT NOT NULL;

CREATE INDEX idx_short_urls_user_id ON short_urls (user_id);

ALTER TABLE short_urls
    ADD CONSTRAINT fk_short_urls_user
    FOREIGN KEY (user_id)
    REFERENCES app_users (id)
    ON DELETE CASCADE;
