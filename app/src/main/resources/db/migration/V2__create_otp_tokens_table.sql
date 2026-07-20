CREATE TABLE IF NOT EXISTS otp_tokens (
    id         UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    code       VARCHAR(10) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used       BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_otp_tokens PRIMARY KEY (id),
    CONSTRAINT fk_otp_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_otp_tokens_user_id ON otp_tokens (user_id);
