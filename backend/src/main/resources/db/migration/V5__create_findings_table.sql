CREATE TABLE findings (
                          id UUID PRIMARY KEY,
                          analysis_id UUID NOT NULL,
                          rule_key VARCHAR(100) NOT NULL,
                          rule_version VARCHAR(30) NOT NULL,
                          fingerprint VARCHAR(64) NOT NULL,
                          title VARCHAR(200) NOT NULL,
                          category VARCHAR(30) NOT NULL,
                          severity VARCHAR(20) NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          file_path VARCHAR(1000) NOT NULL,
                          start_line INTEGER NOT NULL,
                          end_line INTEGER NOT NULL,
                          code_excerpt TEXT NOT NULL,
                          description TEXT NOT NULL,
                          impact TEXT NOT NULL,
                          recommendation TEXT NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          status_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                          CONSTRAINT fk_findings_analysis
                              FOREIGN KEY (analysis_id)
                                  REFERENCES analyses (id)
                                  ON DELETE CASCADE,

                          CONSTRAINT chk_findings_rule_key_not_blank
                              CHECK (LENGTH(TRIM(rule_key)) > 0),

                          CONSTRAINT chk_findings_rule_version_not_blank
                              CHECK (LENGTH(TRIM(rule_version)) > 0),

                          CONSTRAINT chk_findings_fingerprint_length
                              CHECK (LENGTH(fingerprint) = 64),

                          CONSTRAINT chk_findings_title_not_blank
                              CHECK (LENGTH(TRIM(title)) > 0),

                          CONSTRAINT chk_findings_category
                              CHECK (
                                  category IN (
                                               'SECRETS',
                                               'CONFIGURATION',
                                               'CODE',
                                               'DEPENDENCY'
                                      )
                                  ),

                          CONSTRAINT chk_findings_severity
                              CHECK (
                                  severity IN (
                                               'CRITICAL',
                                               'HIGH',
                                               'MEDIUM',
                                               'LOW'
                                      )
                                  ),

                          CONSTRAINT chk_findings_status
                              CHECK (
                                  status IN (
                                             'OPEN',
                                             'RESOLVED',
                                             'ACCEPTED_RISK',
                                             'FALSE_POSITIVE'
                                      )
                                  ),

                          CONSTRAINT chk_findings_file_path_not_blank
                              CHECK (LENGTH(TRIM(file_path)) > 0),

                          CONSTRAINT chk_findings_start_line
                              CHECK (start_line >= 1),

                          CONSTRAINT chk_findings_line_range
                              CHECK (end_line >= start_line)
);

CREATE INDEX idx_findings_analysis_created_at
    ON findings (analysis_id, created_at);

CREATE INDEX idx_findings_analysis_severity
    ON findings (analysis_id, severity);

CREATE INDEX idx_findings_analysis_status
    ON findings (analysis_id, status);

CREATE INDEX idx_findings_analysis_fingerprint
    ON findings (analysis_id, fingerprint);