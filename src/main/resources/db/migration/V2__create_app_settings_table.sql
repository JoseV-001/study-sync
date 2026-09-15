CREATE TABLE app_settings (
    setting_key VARCHAR(64) PRIMARY KEY,
    setting_value VARCHAR(4000) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
