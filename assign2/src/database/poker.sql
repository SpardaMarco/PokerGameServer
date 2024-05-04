-- create schema



DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    username TEXT PRIMARY KEY NOT NULL,
    password TEXT NOT NULL,
    rank INTEGER DEFAULT 0,
    session_token TEXT UNIQUE DEFAULT NULL,
    token_expiration_date DATETIME DEFAULT NULL
);

INSERT INTO users (username, password)
VALUES ('user1', 'password1');
INSERT INTO users (username, password)
VALUES ('user2', 'password2');
INSERT INTO users (username, password)
VALUES ('admin', 'admin_password');
