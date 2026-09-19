CREATE TABLE study_entries (
    clockify_entry_id TEXT PRIMARY KEY,
    project_id TEXT,
    task_id TEXT,
    description TEXT,
    subject TEXT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NOT NULL,
    duration_minutes BIGINT NOT NULL,
    recorded_date DATE NOT NULL,
    synced_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_study_entries_started_at ON study_entries (started_at);
CREATE INDEX idx_study_entries_recorded_date ON study_entries (recorded_date);
CREATE INDEX idx_study_entries_subject ON study_entries (subject);
