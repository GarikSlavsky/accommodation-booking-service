INSERT INTO users (id, first_name, last_name, email, password, is_deleted)
VALUES
    (2, 'Jane', 'Doe', 'jane.doe@example.com', 'hashed_password_2', false),
    (3, 'John', 'Smith', 'john.smith@example.com', 'hashed_password_3', false),
    (4, 'Emily', 'Clark', 'emily.clark@example.com', 'hashed_password_4', false);

INSERT INTO roles (id, name) VALUES (2, 'ROLE_CUSTOMER');

INSERT INTO users_roles (user_id, role_id) VALUES (2, 2), (3, 2), (4, 2);
