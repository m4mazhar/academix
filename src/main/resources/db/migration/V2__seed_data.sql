-- V2__seed_data.sql
-- Default seed data for Academix

-- Main branch
INSERT INTO branch (branch_code, branch_name, status, opened_date)
VALUES ('BR-HQ', 'Main Branch', 'ACTIVE', DATE '2026-01-01');

-- Default super-admin user (password: admin123)
-- BCrypt encoded: $2a$12$oRX.oa6VVIZfE6g4ypV5tOeQJSmkOFn3N0FHC6PkF3UcmJyv.0SBK
INSERT INTO app_user (username, password, full_name, enabled)
VALUES ('admin', '$2a$12$oRX.oa6VVIZfE6g4ypV5tOeQJSmkOFn3N0FHC6PkF3UcmJyv.0SBK', 'System Admin', TRUE);

-- Assign super-admin role to admin user
INSERT INTO user_branch_role (user_id, branch_id, role)
VALUES (1, 1, 'SUPER_ADMIN');
