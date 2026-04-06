ALTER TABLE v1_delivra_service.users
    ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS v1_delivra_service.user_tokens (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER      NOT NULL REFERENCES v1_delivra_service.users (id) ON DELETE CASCADE,
    token       VARCHAR(100) NOT NULL UNIQUE,
    type        VARCHAR(30)  NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    used_at     TIMESTAMP
);
