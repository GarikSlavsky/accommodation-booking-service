INSERT INTO users (id, first_name, last_name, email, password, is_deleted)
VALUES (1, 'Test User', 'Last Name', 'test@example.com', 'hashed_password', false);

INSERT INTO roles (id, name) VALUES (1, 'ROLE_MANAGER');

INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);