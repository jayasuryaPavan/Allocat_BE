-- Create user_settings table for storing user preferences and API keys
CREATE TABLE user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    gemini_api_key VARCHAR(512),  -- Encrypted at application layer
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for faster lookups
CREATE INDEX idx_user_settings_user_id ON user_settings(user_id);

-- Add comment for documentation
COMMENT ON TABLE user_settings IS 'Stores user-specific settings including encrypted AI API keys';
COMMENT ON COLUMN user_settings.gemini_api_key IS 'AES-encrypted Gemini API key for user-specific AI requests';

