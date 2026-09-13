CREATE DATABASE IF NOT EXISTS url_shortener;
USE url_shortener;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(190) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    blocked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_users_email (email)
);

CREATE TABLE IF NOT EXISTS short_urls (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_url VARCHAR(2048) NOT NULL,
    short_code VARCHAR(32) NOT NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_short_urls_short_code (short_code),
    KEY idx_short_urls_user_id (user_id),
    KEY idx_short_urls_created_at (created_at),
    KEY idx_short_urls_click_count (click_count),
    CONSTRAINT fk_short_urls_user
        FOREIGN KEY (user_id)
        REFERENCES app_users (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS click_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    short_url_id BIGINT NOT NULL,
    clicked_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ip_address VARCHAR(64),
    user_agent VARCHAR(512),
    referrer VARCHAR(512),
    PRIMARY KEY (id),
    KEY idx_click_events_clicked_at (clicked_at),
    KEY idx_click_events_short_url_id (short_url_id),
    CONSTRAINT fk_click_events_short_url
        FOREIGN KEY (short_url_id)
        REFERENCES short_urls (id)
        ON DELETE CASCADE
);
