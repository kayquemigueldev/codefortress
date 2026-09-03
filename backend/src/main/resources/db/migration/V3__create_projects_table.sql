CREATE TABLE projects (
                          id UUID PRIMARY KEY,
                          owner_id UUID NOT NULL,
                          name VARCHAR(120) NOT NULL,
                          description VARCHAR(500),
                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                          CONSTRAINT fk_projects_owner
                              FOREIGN KEY (owner_id)
                                  REFERENCES users (id)
                                  ON DELETE CASCADE,

                          CONSTRAINT chk_projects_name_not_blank
                              CHECK (LENGTH(TRIM(name)) > 0),

                          CONSTRAINT chk_projects_status
                              CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE INDEX idx_projects_owner_status
    ON projects (owner_id, status);

CREATE INDEX idx_projects_owner_created_at
    ON projects (owner_id, created_at);