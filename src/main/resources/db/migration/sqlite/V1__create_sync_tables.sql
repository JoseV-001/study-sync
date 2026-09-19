CREATE TABLE weekly_study (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    week_start DATE NOT NULL UNIQUE,
    week_end DATE NOT NULL,
    total_minutes BIGINT NOT NULL,
    notion_time TEXT NOT NULL,
    synced_at TIMESTAMP NOT NULL
);

CREATE TABLE sync_runs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    week_start DATE NOT NULL,
    triggered_by TEXT NOT NULL,
    status TEXT NOT NULL,
    total_minutes BIGINT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP
);

CREATE INDEX idx_sync_runs_week_start ON sync_runs (week_start);
