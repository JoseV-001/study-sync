CREATE TABLE study_goals (
    id SMALLINT PRIMARY KEY,
    daily_minutes INTEGER NOT NULL,
    weekly_minutes INTEGER NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_study_goals_daily_minutes CHECK (daily_minutes BETWEEN 0 AND 1440),
    CONSTRAINT chk_study_goals_weekly_minutes CHECK (weekly_minutes BETWEEN 0 AND 10080)
);
