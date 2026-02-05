-- V1__init.sql
-- Description: Initial database schema for Wit application
-- Tables: users, terms, user_term_agreements, feeds, feed_images, reactions,
--         companions, chat_rooms, chat_participants, chat_messages, 
--         reports, notifications, countries, major_cities

-- ============================================================================
-- 1. Users
-- ============================================================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    public_id BINARY(16) NOT NULL,
    email VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255),
    gender VARCHAR(10),
    nickname VARCHAR(50) NOT NULL,
    profile_image_path VARCHAR(500),
    birth_date DATE,
    status VARCHAR(30) NOT NULL,
    last_login_at TIMESTAMP,
    withdrawn_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_users_public_id UNIQUE (public_id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_provider_id ON users(provider_id);

-- ============================================================================
-- 2. Terms
-- ============================================================================
CREATE TABLE terms (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    version VARCHAR(20) NOT NULL,
    required BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- 3. User Term Agreements
-- ============================================================================
CREATE TABLE user_term_agreements (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    term_id BIGINT NOT NULL,
    agreed BOOLEAN NOT NULL,
    agreed_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_user_term_agreements_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_term_agreements_terms FOREIGN KEY (term_id) REFERENCES terms(id),
    CONSTRAINT uk_user_term_agreements_user_term UNIQUE (user_id, term_id)
);

-- ============================================================================
-- 4. Feeds
-- ============================================================================
CREATE TABLE feeds (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    location_name VARCHAR(255) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    district_id VARCHAR(100) NOT NULL,
    district_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_feeds_users FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_feeds_user_id ON feeds(user_id);
CREATE INDEX idx_feeds_country_district ON feeds(country_code, district_id);
CREATE INDEX idx_feeds_created_at ON feeds(created_at);

-- ============================================================================
-- 5. Feed Images
-- ============================================================================
CREATE TABLE feed_images (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    feed_id BIGINT NOT NULL,
    image_path VARCHAR(500) NOT NULL,
    image_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_feed_images_feeds FOREIGN KEY (feed_id) REFERENCES feeds(id)
);

CREATE INDEX idx_feed_images_feed_id ON feed_images(feed_id);

-- ============================================================================
-- 6. Reactions
-- ============================================================================
CREATE TABLE reactions (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    feed_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_reactions_feeds FOREIGN KEY (feed_id) REFERENCES feeds(id),
    CONSTRAINT fk_reactions_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_reactions_feed_user UNIQUE (feed_id, user_id)
);

CREATE INDEX idx_reactions_feed_user ON reactions(feed_id, user_id);
CREATE INDEX idx_reactions_user_id ON reactions(user_id);

-- ============================================================================
-- 7. Companions
-- ============================================================================
CREATE TABLE companions (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    location_name VARCHAR(255) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    meeting_date TIMESTAMP NOT NULL,
    max_participants INT NOT NULL,
    age_condition VARCHAR(20) NOT NULL,
    gender_condition VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_companions_users FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_companions_user_id ON companions(user_id);
CREATE INDEX idx_companions_country ON companions(country_code);
CREATE INDEX idx_companions_meeting_date ON companions(meeting_date);

-- ============================================================================
-- 8. Chat Rooms
-- ============================================================================
CREATE TABLE chat_rooms (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    companion_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_chat_rooms_companions FOREIGN KEY (companion_id) REFERENCES companions(id),
    CONSTRAINT uk_chat_rooms_companion UNIQUE (companion_id)
);

CREATE INDEX idx_chat_rooms_companion_id ON chat_rooms(companion_id);

-- ============================================================================
-- 9. Chat Participants
-- ============================================================================
CREATE TABLE chat_participants (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    chat_room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    left_at TIMESTAMP,
    
    CONSTRAINT fk_chat_participants_chat_rooms FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id),
    CONSTRAINT fk_chat_participants_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_chat_participants_room_user UNIQUE (chat_room_id, user_id)
);

CREATE INDEX idx_chat_participants_chat_room_user ON chat_participants(chat_room_id, user_id);
CREATE INDEX idx_chat_participants_user_id ON chat_participants(user_id);

-- ============================================================================
-- 10. Chat Messages
-- ============================================================================
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    chat_room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_chat_messages_chat_rooms FOREIGN KEY (chat_room_id) REFERENCES chat_rooms(id),
    CONSTRAINT fk_chat_messages_users FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE INDEX idx_chat_messages_chat_room_created ON chat_messages(chat_room_id, created_at);
CREATE INDEX idx_chat_messages_sender_id ON chat_messages(sender_id);

-- ============================================================================
-- 11. Reports
-- ============================================================================
CREATE TABLE reports (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    report_code VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    
    CONSTRAINT fk_reports_users FOREIGN KEY (reporter_id) REFERENCES users(id)
);

CREATE INDEX idx_reports_target ON reports(target_type, target_id);
CREATE INDEX idx_reports_reporter ON reports(reporter_id);
CREATE INDEX idx_reports_status ON reports(status);

-- ============================================================================
-- 12. Notifications
-- ============================================================================
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(500) NOT NULL,
    target_type VARCHAR(20),
    target_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notifications_users FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_notifications_user_read_created ON notifications(user_id, is_read, created_at);

-- ============================================================================
-- 13. Countries (Reference Data)
-- ============================================================================
CREATE TABLE countries (
    code VARCHAR(2) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    center_lat DECIMAL(10, 8) NOT NULL,
    center_lng DECIMAL(11, 8) NOT NULL,
    bounds_sw_lat DECIMAL(10, 8) NOT NULL,
    bounds_sw_lng DECIMAL(11, 8) NOT NULL,
    bounds_ne_lat DECIMAL(10, 8) NOT NULL,
    bounds_ne_lng DECIMAL(11, 8) NOT NULL,
    zoom_level INT NOT NULL
);

-- ============================================================================
-- 14. Major Cities (Reference Data)
-- ============================================================================
CREATE TABLE major_cities (
    code VARCHAR(100) PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    center_lat DECIMAL(10, 8) NOT NULL,
    center_lng DECIMAL(11, 8) NOT NULL,
    zoom_level INT NOT NULL,
    
    CONSTRAINT fk_major_cities_countries FOREIGN KEY (country_code) REFERENCES countries(code)
);

CREATE INDEX idx_major_cities_country_code ON major_cities(country_code);

-- ============================================================================
-- Rollback (for reference only)
-- ============================================================================
-- DROP TABLE IF EXISTS major_cities;
-- DROP TABLE IF EXISTS countries;
-- DROP TABLE IF EXISTS notifications;
-- DROP TABLE IF EXISTS reports;
-- DROP TABLE IF EXISTS chat_messages;
-- DROP TABLE IF EXISTS chat_participants;
-- DROP TABLE IF EXISTS chat_rooms;
-- DROP TABLE IF EXISTS companions;
-- DROP TABLE IF EXISTS reactions;
-- DROP TABLE IF EXISTS feed_images;
-- DROP TABLE IF EXISTS feeds;
-- DROP TABLE IF EXISTS user_term_agreements;
-- DROP TABLE IF EXISTS terms;
-- DROP TABLE IF EXISTS users;
