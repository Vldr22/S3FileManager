CREATE TABLE IF NOT EXISTS roles (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(255),
                                     password VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users_roles (
                                           user_id BIGINT NOT NULL,
                                           roles_id BIGINT NOT NULL,
                                           PRIMARY KEY (user_id, roles_id),
                                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                           FOREIGN KEY (roles_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS files (
                                     id BIGSERIAL PRIMARY KEY,
                                     unique_name VARCHAR(64) NOT NULL UNIQUE,
                                     original_name VARCHAR(1024) NOT NULL,
                                     type VARCHAR(255),
                                     size BIGINT NOT NULL,
                                     file_hash VARCHAR(64) NOT NULL UNIQUE
);
