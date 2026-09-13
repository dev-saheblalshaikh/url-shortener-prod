USE url_shortener;

SET @role_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'app_users'
      AND column_name = 'role'
);
SET @sql = IF(
    @role_exists = 0,
    'ALTER TABLE app_users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT ''USER''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @blocked_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'app_users'
      AND column_name = 'blocked'
);
SET @sql = IF(
    @blocked_exists = 0,
    'ALTER TABLE app_users ADD COLUMN blocked BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE app_users SET role = 'USER' WHERE role IS NULL OR role = '';
UPDATE app_users SET blocked = FALSE WHERE blocked IS NULL;

-- First admin account for project/demo use.
-- Email: admin@example.com
-- Password is also enforced by AdminBootstrap on app startup: admin123
INSERT INTO app_users (email, password_hash, role, blocked)
SELECT 'admin@example.com', '$2a$10$8Yx4I0qOCb5hu1Zz8wx5A.qtbsqLfYz0/EqLXId/F0kwcBg46jDLu', 'ADMIN', FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM app_users WHERE email = 'admin@example.com'
);

UPDATE app_users
SET role = 'ADMIN', blocked = FALSE
WHERE email = 'admin@example.com';
