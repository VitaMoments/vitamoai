CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS id_uuid UUID,
    ADD COLUMN IF NOT EXISTS display_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS hashed_password VARCHAR(255),
    ADD COLUMN IF NOT EXISTS first_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS alias VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bio VARCHAR(500),
    ADD COLUMN IF NOT EXISTS birth_date DATE,
    ADD COLUMN IF NOT EXISTS role VARCHAR(50),
    ADD COLUMN IF NOT EXISTS created_at BIGINT,
    ADD COLUMN IF NOT EXISTS updated_at BIGINT,
    ADD COLUMN IF NOT EXISTS deleted_at BIGINT;

UPDATE users
SET
    id_uuid = COALESCE(id_uuid, gen_random_uuid()),
    display_name = COALESCE(display_name, username),
    hashed_password = COALESCE(hashed_password, password, ''),
    role = COALESCE(role, 'USER'),
    created_at = COALESCE(created_at, EXTRACT(EPOCH FROM NOW())::BIGINT),
    updated_at = COALESCE(updated_at, EXTRACT(EPOCH FROM NOW())::BIGINT);

ALTER TABLE users
    ALTER COLUMN id_uuid SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN display_name SET NOT NULL,
    ALTER COLUMN hashed_password SET NOT NULL,
    ALTER COLUMN role SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

CREATE INDEX IF NOT EXISTS users_display_name_idx ON users (display_name);
CREATE UNIQUE INDEX IF NOT EXISTS users_email_uidx ON users (email);

ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS user_id_uuid UUID;

UPDATE refresh_tokens rt
SET user_id_uuid = u.id_uuid
FROM users u
WHERE rt.user_id_uuid IS NULL
  AND rt.user_id = u.id;

ALTER TABLE refresh_tokens
    ALTER COLUMN user_id_uuid SET NOT NULL;

DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    SELECT tc.constraint_name
    INTO constraint_name
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
      ON tc.constraint_name = kcu.constraint_name
    WHERE tc.table_name = 'refresh_tokens'
      AND tc.constraint_type = 'FOREIGN KEY'
      AND kcu.column_name = 'user_id'
    LIMIT 1;

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE refresh_tokens DROP CONSTRAINT %I', constraint_name);
    END IF;
END $$;

ALTER TABLE refresh_tokens
    DROP COLUMN IF EXISTS user_id;

ALTER TABLE refresh_tokens
    RENAME COLUMN user_id_uuid TO user_id;

CREATE INDEX IF NOT EXISTS refresh_tokens_user_id_idx ON refresh_tokens (user_id);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'users'
          AND constraint_name = 'users_pkey'
    ) THEN
        ALTER TABLE users DROP CONSTRAINT users_pkey;
    END IF;
END $$;

ALTER TABLE users
    DROP COLUMN IF EXISTS id;

ALTER TABLE users
    RENAME COLUMN id_uuid TO id;

ALTER TABLE users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE users
    DROP COLUMN IF EXISTS username,
    DROP COLUMN IF EXISTS password;

DROP INDEX IF EXISTS users_username_uidx;

ALTER TABLE refresh_tokens
    ADD CONSTRAINT refresh_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

