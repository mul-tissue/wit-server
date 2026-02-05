-- V2__update_users_schema.sql
-- Description: Update users table to match User entity design
-- Changes:
--   - Remove public_id (not using UUID)
--   - Change provider -> social_type
--   - Change provider_id format
--   - Add role column
--   - Change status values
--   - Change birth_date -> birth_year (integer)
--   - Change profile_image_path -> profile_image_url
--   - Remove last_login_at, withdrawn_at

-- Drop existing users table (temporary solution for development)
-- Note: In production, you should use ALTER TABLE for migration
DROP TABLE IF EXISTS user_term_agreements;
DROP TABLE IF EXISTS chat_participants;
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS reactions;
DROP TABLE IF EXISTS feeds;
DROP TABLE IF EXISTS feed_images;
DROP TABLE IF EXISTS companions;
DROP TABLE IF EXISTS chat_rooms;
DROP TABLE IF EXISTS users;

-- Recreate users table with new schema
CREATE TABLE users (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    social_type VARCHAR(20) NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    gender VARCHAR(10),
    birth_year INT,
    profile_image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_social_provider UNIQUE (social_type, provider_id)
);

CREATE INDEX idx_users_social_type_provider_id ON users(social_type, provider_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
