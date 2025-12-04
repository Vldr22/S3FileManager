INSERT INTO roles (id, name) VALUES
                                 (1, 'ROLE_ADMIN'),
                                 (2, 'ROLE_USER'),
                                 (3, 'ROLE_USER_ADDED')
ON CONFLICT (id) DO NOTHING;