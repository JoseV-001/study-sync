CREATE TABLE subject_goals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject TEXT NOT NULL UNIQUE,
    weekly_minutes INTEGER NOT NULL CHECK (weekly_minutes BETWEEN 1 AND 10080),
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_subject_goals_subject ON subject_goals (subject);
