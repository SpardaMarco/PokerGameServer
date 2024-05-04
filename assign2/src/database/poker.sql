DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    username TEXT PRIMARY KEY NOT NULL,
    password TEXT NOT NULL,
    rank INTEGER DEFAULT 0,
    session_token TEXT UNIQUE DEFAULT NULL,
    session_expiration DATETIME DEFAULT NULL
);
