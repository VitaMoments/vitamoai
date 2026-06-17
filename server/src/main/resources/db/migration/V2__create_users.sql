CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS users_email_uidx ON users (email);
CREATE UNIQUE INDEX IF NOT EXISTS users_username_uidx ON users (username);
