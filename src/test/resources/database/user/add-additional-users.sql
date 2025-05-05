INSERT INTO users (id, first_name, last_name, email, password, is_deleted)
VALUES
    (3, 'Jane', 'Doe', 'jane.doe@example.com', 'hashed_password_2', false),
    (4, 'John', 'Smith', 'john.smith@example.com', 'hashed_password_3', false),
    (5, 'Emily', 'Clark', 'emily.clark@example.com', 'hashed_password_4', false);

INSERT INTO users_roles (user_id, role_id) VALUES (2, 2), (3, 2), (4, 2);
