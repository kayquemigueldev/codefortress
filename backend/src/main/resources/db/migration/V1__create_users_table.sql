CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(320) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       display_name VARCHAR(120) NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                       CONSTRAINT uq_users_email UNIQUE (email),
                       CONSTRAINT chk_users_email_normalized
                           CHECK (email = LOWER(email)),
                       CONSTRAINT chk_users_status
                           CHECK (status IN ('ACTIVE', 'DISABLED'))
);
CREATE INDEX idx_users_status ON users (status);
