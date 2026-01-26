CREATE TABLE users
(
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(80) NOT NULL,
    email VARCHAR(50) UNIQUE,
    created TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registration_status VARCHAR(30) NOT NULL,
    last_login TIMESTAMP,
    deleted BOOLEAN      NOT NULL default false


);


CREATE TABLE roles
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    user_system_role VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(50) NOT NULL

);

CREATE TABLE users_roles
(
    user_id BIGINT NOT NULL,
    role_id INTEGER NOT NULL,

    PRIMARY KEY(user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE refresh_token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(128) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_refresh_token UNIQUE (id, user_id)

);


INSERT INTO users(username, password, email, created, updated, registration_status, last_login, deleted)
VALUES ('super_admin', '$2a$10$rBLnOMt6NmClhVia6EfEm.fDuFvJv7hYkG5N1.ewREBUYywDOmmqy', 'super_admin@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', CURRENT_TIMESTAMP, false),
       ('admin', '$2a$10$jmm/j.TH.Gtd0WTDFTLiMOIcXv1W/tqT062xtnISIIWjV7tjT9YJq', 'admin@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', CURRENT_TIMESTAMP, false),
       ('user', '$2a$10$xDds0w2Cowtv8J9Xk4gcvOaPBpNbsvhNDV.4XW61wwtZoyTrBRUFC', 'user@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', CURRENT_TIMESTAMP, false);


INSERT INTO roles (name, user_system_role, created_by)
VALUES
                ('SUPER_ADMIN', 'SUPER_ADMIN','SUPER_ADMIN'),
                ('ADMIN', 'ADMIN','SUPER_ADMIN'),
                ('DRIVER', 'DRIVER','SUPER_ADMIN'),
                ('DISPATCHER', 'DISPATCHER', 'SUPER_ADMIN');


INSERT INTO users_roles (user_id, role_id) VALUES
                                               (1, 1),
                                               (2, 2),
                                               (3, 3);
