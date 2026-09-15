CREATE TABLE study_entries (
    clockify_entry_id VARCHAR(64) PRIMARY KEY,
    project_id VARCHAR(64),
    task_id VARCHAR(64),
    description VARCHAR(2000),
    subject VARCHAR(256) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes BIGINT NOT NULL,
    recorded_date DATE NOT NULL,
    synced_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_study_entries_started_at ON study_entries (started_at);
CREATE INDEX idx_study_entries_recorded_date ON study_entries (recorded_date);
CREATE INDEX idx_study_entries_subject ON study_entries (subject);
