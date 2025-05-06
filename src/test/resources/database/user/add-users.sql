INSERT INTO users (id, first_name, last_name, email, password, is_deleted)
VALUES (2, 'Test User', 'Last Name', 'test@example.com', 'hashed_password', false);

INSERT INTO roles (id, name) VALUES (1, 'ROLE_MANAGER'),
                                    (2, 'ROLE_CUSTOMER');

INSERT INTO users_roles (user_id, role_id) VALUES (2, 1);
