-- V1__init.sql
-- Description: Initial database schema for Wit application
-- Tables: users, terms, user_terms_agreements, event_publication

-- ============================================================================
-- 1. Spring Modulith Event Publication
-- ============================================================================
CREATE TABLE event_publication (
    id UUID PRIMARY KEY,
    listener_id VARCHAR(512) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP NOT NULL,
    completion_date TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    completion_attempts INT NOT NULL DEFAULT 0,
    last_resubmission_date TIMESTAMP
);

CREATE INDEX idx_event_publication_completion_date ON event_publication(completion_date);
CREATE INDEX idx_event_publication_publication_date ON event_publication(publication_date);
CREATE INDEX idx_event_publication_status ON event_publication(status);

-- ============================================================================
-- 2. Users
-- ============================================================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    public_id VARCHAR(26) NOT NULL,
    social_type VARCHAR(20) NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    gender VARCHAR(10),
    birth_date DATE,
    profile_image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_users_public_id UNIQUE (public_id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_social_provider UNIQUE (social_type, provider_id)
);

CREATE INDEX idx_users_public_id ON users(public_id);
CREATE INDEX idx_users_social_type_provider_id ON users(social_type, provider_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);

-- ============================================================================
-- 3. Terms
-- ============================================================================
CREATE TABLE terms (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    public_id VARCHAR(26) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    version VARCHAR(20) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_terms_public_id UNIQUE (public_id)
);

CREATE INDEX idx_terms_public_id ON terms(public_id);
CREATE INDEX idx_terms_type ON terms(type);
CREATE INDEX idx_terms_active ON terms(active);
CREATE INDEX idx_terms_type_version ON terms(type, version);

-- ============================================================================
-- 4. User Terms Agreements
-- ============================================================================
CREATE TABLE user_terms_agreements (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    term_id BIGINT NOT NULL,
    agreed BOOLEAN NOT NULL DEFAULT FALSE,
    agreed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_terms_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_terms_terms FOREIGN KEY (term_id) REFERENCES terms(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_terms UNIQUE (user_id, term_id)
);

CREATE INDEX idx_user_terms_user_id ON user_terms_agreements(user_id);
CREATE INDEX idx_user_terms_term_id ON user_terms_agreements(term_id);

-- ============================================================================
-- Initial Data: Default Terms
-- ============================================================================
INSERT INTO terms (public_id, type, title, version, required, active) VALUES
('01JKTERMS001SERVICE00001', 'TERMS_OF_SERVICE', '서비스 이용약관', 'v1.0', TRUE, TRUE),
('01JKTERMS002PRIVACY00001', 'PRIVACY_POLICY', '개인정보 처리방침', 'v1.0', TRUE, TRUE),
('01JKTERMS003MARKETNG0001', 'MARKETING', '마케팅 정보 수신 동의', 'v1.0', FALSE, TRUE);
