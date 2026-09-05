CREATE TABLE analyses (
                          id UUID PRIMARY KEY,
                          project_id UUID NOT NULL,
                          sequence_number INTEGER NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          source_type VARCHAR(20) NOT NULL,
                          source_reference VARCHAR(255) NOT NULL,
                          source_filename VARCHAR(255) NOT NULL,
                          rule_set_version VARCHAR(30) NOT NULL,
                          score_version VARCHAR(30) NOT NULL,
                          security_score SMALLINT,
                          files_scanned INTEGER,
                          lines_scanned BIGINT,
                          findings_count INTEGER,
                          started_at TIMESTAMP WITH TIME ZONE,
                          completed_at TIMESTAMP WITH TIME ZONE,
                          failure_code VARCHAR(50),
                          failure_message VARCHAR(500),
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                          CONSTRAINT fk_analyses_project
                              FOREIGN KEY (project_id)
                                  REFERENCES projects (id),

                          CONSTRAINT uq_analyses_project_sequence
                              UNIQUE (project_id, sequence_number),

                          CONSTRAINT chk_analyses_sequence_number
                              CHECK (sequence_number >= 1),

                          CONSTRAINT chk_analyses_status
                              CHECK (
                                  status IN (
                                             'QUEUED',
                                             'RUNNING',
                                             'COMPLETED',
                                             'FAILED',
                                             'CANCELLED'
                                      )
                                  ),

                          CONSTRAINT chk_analyses_source_type
                              CHECK (source_type IN ('UPLOAD')),

                          CONSTRAINT chk_analyses_security_score
                              CHECK (
                                  security_score IS NULL
                                      OR security_score BETWEEN 0 AND 100
                                  ),

                          CONSTRAINT chk_analyses_files_scanned
                              CHECK (
                                  files_scanned IS NULL
                                      OR files_scanned >= 0
                                  ),

                          CONSTRAINT chk_analyses_lines_scanned
                              CHECK (
                                  lines_scanned IS NULL
                                      OR lines_scanned >= 0
                                  ),

                          CONSTRAINT chk_analyses_findings_count
                              CHECK (
                                  findings_count IS NULL
                                      OR findings_count >= 0
                                  )
);

CREATE INDEX idx_analyses_project_created_at
    ON analyses (project_id, created_at);

CREATE INDEX idx_analyses_project_status
    ON analyses (project_id, status);