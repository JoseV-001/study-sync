CREATE TABLE study_goals (
    id INTEGER PRIMARY KEY,
    daily_minutes INTEGER NOT NULL CHECK (daily_minutes BETWEEN 0 AND 1440),
    weekly_minutes INTEGER NOT NULL CHECK (weekly_minutes BETWEEN 0 AND 10080),
    updated_at TIMESTAMP NOT NULL
);
