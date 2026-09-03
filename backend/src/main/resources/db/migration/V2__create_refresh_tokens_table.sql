CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                user_id UUID NOT NULL,
                                family_id UUID NOT NULL,
                                token_hash VARCHAR(64) NOT NULL,
                                expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                revoked_at TIMESTAMP WITH TIME ZONE,
                                replaced_by_token_id UUID,

                                CONSTRAINT uq_refresh_tokens_hash
                                    UNIQUE (token_hash),

                                CONSTRAINT fk_refresh_tokens_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users (id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_refresh_tokens_replacement
                                    FOREIGN KEY (replaced_by_token_id)
                                        REFERENCES refresh_tokens (id),

                                CONSTRAINT chk_refresh_tokens_expiration
                                    CHECK (expires_at > created_at)
);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_tokens_family_id
    ON refresh_tokens (family_id);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens (expires_at);